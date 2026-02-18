package org.example.task

import org.example.task.dtos.TaskEventDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class TaskEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, TaskEventDto>,
){
    private val TOPIC_NAME = "task-update-topic"

    fun sendTaskEvent(event: TaskEventDto){
        kafkaTemplate.send(TOPIC_NAME, event.task.taskId.toString(), event)
    }
}