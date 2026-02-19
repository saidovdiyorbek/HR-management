package uz.zero.notification.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import uz.zero.notification.TaskActionService
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