package uz.zero.notification.listeners

import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uz.zero.notification.AuthUserClient
import uz.zero.notification.NotConnectedTelegramBotException
import uz.zero.notification.TaskActionService
import uz.zero.notification.TelegramFeignClient
import uz.zero.notification.dtos.SendMessageRequest
import uz.zero.notification.dtos.TaskActionEvent
import uz.zero.notification.dtos.TaskEventDto

@Component
class KafkaNotificationListener(
    private val notificationService: TaskActionService
){
    @KafkaListener(topics = ["task-update-topic"], groupId = "notification-group")
    fun listen(event: TaskEventDto){
        notificationService.processTaskEvent(event)
    }
}