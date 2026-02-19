package uz.zero.notification

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

        val user = try {
            authUserClient.getUserShortInfo(event.userId)
        } catch (e: FeignClientException) {
            throw e
        }

        val organization = try {
            organizationClient.getOrganizationInfo(event.userId)
        } catch (e: FeignClientException) {
            throw e
        }

        val project = try {
            projectClient.getProjectShortInfoByBoardId(event.task.boardId)
        } catch (e: FeignClientException) {
            throw e
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
                sendMessage.parseMode = "HTML"
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
        if (entity == null || entity.expriTime.isBefore(java.time.LocalDateTime.now())) {
            throw HashNotValidException()
        }
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
        val userId= hashService.checkHashAndReturnUserId(hash)
        var user = repository.findByUserIdAndDeletedIsFalse(userId)

        if(user == null) {
            user = UserTelegram(
                chatId = from.id,
                firstName = from.firstName,
                lastName = from.lastName,
                username = from.userName,
                userId = userId
            )
        }else{
            user.chatId = from.id
            user.firstName = from.firstName
            user.lastName = from.lastName
            user.username = from.userName
        }

        repository.save(user)
    }
}