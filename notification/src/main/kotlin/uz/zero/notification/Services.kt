package uz.zero.notification

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User
import uz.zero.notification.bot.Bot
import uz.zero.notification.bot.BotMessage
import uz.zero.notification.bot.BotProperties
import uz.zero.notification.dtos.OrganizationInfo
import uz.zero.notification.dtos.ProjectShortInfo
import uz.zero.notification.dtos.TaskEventDto
import uz.zero.notification.dtos.UserShortInfo
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

interface TaskActionService {
    fun processTaskEvent(event: TaskEventDto)
}

@Service
class TaskActionServiceImpl(
    private val authUserClient: AuthUserClient,
    private val repository: NotificationRepository,
    private val bot: Bot,
    private val organizationClient: OrganizationClient,
    private val userNotificationRepository: UserNotificationRepository,
    private val userTelegramRepository: UserTelegramRepository,
    private val projectClient: ProjectClient,
    private val message: BotMessage,
    @Value("\${task.url}") val taskUrl: String,
) : TaskActionService {

    override fun processTaskEvent(event: TaskEventDto) {
        logger.info { "📩 Kafka event keldi: action=${event.action}, taskId=${event.task.taskId}, userId=${event.userId}" }

        val user = try {
            authUserClient.getUserShortInfo(event.userId)
        } catch (e: Exception) {
            logger.error { "❌ AuthUserClient xatosi: $e" }
            null
        }

        val organization = try {
            organizationClient.getOrganizationInfo(event.userId)
        } catch (e: Exception) {
            logger.error { "❌ OrganizationClient xatosi: $e" }
            null
        }

        val project = try {
            projectClient.getProjectShortInfoByBoardId(event.task.boardId)
        } catch (e: Exception) {
            logger.error { "❌ ProjectClient xatosi: $e" }
            null
        }

        if (user == null || organization == null || project == null) {
            logger.warn { "⚠️ Tashqi servislardan ma'lumot olinmadi. Notification saqlanmaydi." }
            return
        }

        when (event.action) {
            ActionType.CREATED -> handleCreated(event, user, organization, project)
            ActionType.UPDATED -> handleUpdated(event, user, organization, project)
        }
    }

    private fun handleCreated(
        event: TaskEventDto,
        user: UserShortInfo,
        organization: OrganizationInfo,
        project: ProjectShortInfo,
    ) {
        val recipientIds = event.actionDetails?.addedEmployeeIds ?: emptyList()

        val text = message.buildMessage(
            date = LocalDateTime.now(),
            organizationName = organization.name,
            projectName = project.projectName,
            actionOwner = user.fullName,
            title = event.task.title,
            taskUrl = taskUrl + event.task.taskId,
            lines = listOf("task yaratildi va siz taskka ulandingiz"),
        )

        val notification = Notification(
            companyId = organization.id,
            companyName = organization.name,
            projectId = project.projectId,
            projectName = project.projectName,
            taskId = event.task.taskId,
            taskName = event.task.title,
            actionType = ActionType.CREATED,
            message = text,
        )
        val savedNotification = repository.save(notification)

        sendToUsers(savedNotification, recipientIds, text)
    }

    private fun handleUpdated(
        event: TaskEventDto,
        user: UserShortInfo,
        organization: OrganizationInfo,
        project: ProjectShortInfo,
    ) {
        val details = event.actionDetails
        val lines = mutableListOf<String>()

        if (details?.fromState != null && details.toState != null) {
            val fromName = project.board.states.find { it.id == details.fromState }?.name ?: "Noma'lum"
            val toName = project.board.states.find { it.id == details.toState }?.name ?: "Noma'lum"
            lines += "$fromName >> $toName"
        }

        if (details?.title != null) {
            lines += "sarlavha o'zgartirildi: \"${details.title}\""
        }

        if (details?.attachesHashes != null) {
            lines += "fayl biriktirildi (${details.attachesHashes!!.size} ta)"
        }

        if (lines.isEmpty()) lines += "task yangilandi"

        val updateText = message.buildMessage(
            date = LocalDateTime.now(),
            organizationName = organization.name,
            projectName = project.projectName,
            actionOwner = user.fullName,
            title = event.task.title,
            taskUrl = taskUrl + event.task.taskId,
            lines = lines,
        )

        val notification = Notification(
            companyId = organization.id,
            companyName = organization.name,
            projectId = project.projectId,
            projectName = project.projectName,
            taskId = event.task.taskId,
            taskName = event.task.title,
            actionType = ActionType.UPDATED,
            message = updateText,
        )
        val savedNotification = repository.save(notification)

        val assignedIds = event.task.assignedEmployeesIds ?: emptyList()
        sendToUsers(savedNotification, assignedIds, updateText)

        val addedIds = details?.addedEmployeeIds ?: emptyList()
        if (addedIds.isNotEmpty()) {
            val joinText = message.buildMessage(
                date = LocalDateTime.now(),
                organizationName = organization.name,
                projectName = project.projectName,
                actionOwner = user.fullName,
                title = event.task.title,
                taskUrl = taskUrl + event.task.taskId,
                lines = listOf("siz taskka ulandingiz"),
            )
            sendToUsers(savedNotification, addedIds, joinText)
        }
    }

    private fun sendToUsers(
        notification: Notification,
        userIds: List<Long>,
        text: String,
    ) {
        for (userId in userIds) {
            val userTelegram = userTelegramRepository.findByUserIdAndDeletedIsFalse(userId) ?: continue

            val status = try {
                val sendMessage = SendMessage()
                sendMessage.chatId = userTelegram.chatId.toString()
                sendMessage.text = text
                sendMessage.parseMode = org.telegram.telegrambots.meta.api.methods.ParseMode.HTML
                bot.execute(sendMessage)
                NotificationStatus.SENT
            } catch (e: Exception) {
                NotificationStatus.FAILED
            }

            userNotificationRepository.save(
                UserNotification(
                    userTelegram = userTelegram,
                    notification = notification,
                    status = status,
                )
            )
        }
    }
}



interface HashService {
    fun generateHash(): String
    fun checkHashAndReturnUserId(hash: String): Long
    fun removeHash(hash: String)
}

@Service
class HashServiceImpl(
    val securityUtil: SecurityUtil,
    val repository: HashRepository,
    @Value("\${hash.expiration}") val expirationTime: Long,
    @Value("\${hash.telegram.url}") val telegramUrl: String,
    val botProperties: BotProperties
) : HashService {
    override fun generateHash(): String {
        val userId = securityUtil.getCurrentUserId()
        val hash = java.util.UUID.randomUUID().toString()
        val hashEntity = Hash(
            userId = userId,
            hash = hash,
            expriTime = java.time.LocalDateTime.now().plusMinutes(expirationTime),
            url = telegramUrl.plus("${botProperties.username}?start=$hash"),
        )
        repository.save(hashEntity)
        return hashEntity.url
    }

    override fun checkHashAndReturnUserId(hash: String): Long {
        val entity = repository.findByHashAndDeletedFalse(hash)
        if (entity == null || entity.expriTime.isBefore(java.time.LocalDateTime.now()) || entity.isUsed) {
            throw HashNotValidException()
        }
        entity.isUsed = true
        repository.save(entity)
        return entity.userId
    }

    override fun removeHash(hash: String) {
        repository.deleteByHash(hash)
    }

}



interface UserTelegramService {
    fun createOrUpdate(hash: String, from: User)
}

@Service
class UserTelegramImpl(
    val repository: UserTelegramRepository,
    val hashService: HashService,
) : UserTelegramService {
    override fun createOrUpdate(hash: String, from: User) {
        val userId = hashService.checkHashAndReturnUserId(hash)

        val existingByChatId = repository.findByChatId(from.id)
        val existingByUserId = repository.findByUserIdAndDeletedIsFalse(userId)

        val user = when {
            existingByChatId != null -> existingByChatId.apply {
                this.userId = userId
                this.firstName = from.firstName
                this.lastName = from.lastName
                this.username = from.userName
                this.deleted = false
            }
            existingByUserId != null -> existingByUserId.apply {
                this.chatId = from.id
                this.firstName = from.firstName
                this.lastName = from.lastName
                this.username = from.userName
            }
            else -> UserTelegram(
                chatId = from.id,
                firstName = from.firstName,
                lastName = from.lastName,
                username = from.userName,
                userId = userId
            )
        }

        repository.save(user)
    }
}