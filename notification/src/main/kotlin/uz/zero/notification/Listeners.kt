package uz.zero.notification

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaNotificationListener(
    private val notificationService: TaskActionService
){
    @KafkaListener(topics = ["task-update-topic"], groupId = "notification-group")
    fun listen(event: TaskEventDto){
        notificationService.processTaskEvent(event)
    }
}