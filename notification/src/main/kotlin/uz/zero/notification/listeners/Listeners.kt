package uz.zero.notification.listeners

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uz.zero.notification.AuthUserClient
import uz.zero.notification.NotConnectedTelegramBotException
import uz.zero.notification.TaskAction
import uz.zero.notification.TaskActionRepository
import uz.zero.notification.TelegramFeignClient
import uz.zero.notification.dtos.SendMessageRequest
import uz.zero.notification.dtos.TaskActionEvent

@Component
class TaskActionListener(
    private val actionRepository: TaskActionRepository,
    private val telegramClient: TelegramFeignClient,
    private val userClient: AuthUserClient,
) {

    @Async
    @EventListener
    fun handleAction(event: TaskActionEvent) {

        val userShortInfo = userClient.getUserShortInfo(event.userId)
        if (userShortInfo.telegramChatId == null) {
            throw NotConnectedTelegramBotException()
        }

        val action = TaskAction(
            taskId = event.taskId,
            userId = event.userId,
            type = event.actionType,
            details = event.details
        )
        actionRepository.save(action)

        val message = """
            🔥 <b>Task o'zgarishi!</b>
            🆔 Task ID: ${event.taskId}
            👤 User ID: ${event.userId}
            action: ${event.actionType}
            📝 Batafsil: ${event.details ?: "Yo'q"}
        """.trimIndent()

        val telegramMessage = SendMessageRequest(userShortInfo.telegramChatId, message)

        try {

            telegramClient.sendMessage(telegramMessage)
        } catch (e: Exception) {
            println("Telegramga yuborishda xatolik: ${e.message}")
        }
    }
}