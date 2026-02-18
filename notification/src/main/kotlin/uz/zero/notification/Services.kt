package uz.zero.notification

import org.springframework.stereotype.Service
import uz.zero.notification.dtos.SendMessageRequest
import uz.zero.notification.dtos.TaskActionCreateDto
import uz.zero.notification.dtos.TaskActionEvent
import uz.zero.notification.dtos.TaskEventDto
import uz.zero.notification.listeners.TaskActionListener

interface TaskActionService {
    fun create(taskActionCreate: TaskActionCreateDto)
    fun processTaskEvent(event: TaskEventDto)
}

@Service
class TaskActionServiceImpl(
    private val taskListener: TaskActionListener,
    private val authUserClient: AuthUserClient,
    private val telegramClient: TelegramFeignClient,
    private val organizationClient: OrganizationClient,
    private val securityUtil: SecurityUtil,
    private val notificationLogRepository: NotificationRepository,
) : TaskActionService {
    override fun create(taskActionCreate: TaskActionCreateDto) {
        taskActionCreate.run {
            taskListener.handleAction(
                TaskActionEvent(
                    this.taskId,
                    this.userId,
                    this.type,
                    this.details,
                )
            )
        }
    }

    override fun processTaskEvent(event: TaskEventDto) {



        val user = try {
            authUserClient.getUserShortInfo(event.userId)
        } catch (e: FeignClientException) {
            saveLog(event, "User info topilmadi", NotificationStatus.FAILED)
            throw e
        }
        val organization = try {
            organizationClient.getOrganizationInfo(event.userId)
        }catch (e: FeignClientException) {
            throw e
        }

        if (user.telegramChatId == null) {
            println("⚠️ User (ID: ${event.userId}) Telegramga ulanmagan.")
            saveLog(event, "Chat ID yo'q", NotificationStatus.FAILED)
            return
        }


        val messageText = """
            🔥 <b>Task o'zgarishi!</b>
            🏢 Tashkilot nomi: ${organization.name}
            👤 Xodim: ${user.fullName}
            🛠 Holat: ${event.action}
        """.trimIndent()


        try {
            telegramClient.sendMessage(SendMessageRequest(user.telegramChatId, messageText))


            saveLog(event, messageText, NotificationStatus.SENT)
            println("✅ Telegramga ketdi!")

        } catch (e: Exception) {

            println(" Telegram Error: ${e.message}")
            saveLog(event, messageText, NotificationStatus.FAILED)
        }
    }


    private fun saveLog(event: TaskEventDto, text: String, status: NotificationStatus) {
        val log = NotificationLog(
            userId = event.userId,
            taskId = event.task.taskId!!,
            message = text,
            status = status
        )
        notificationLogRepository.save(log)
    }
}