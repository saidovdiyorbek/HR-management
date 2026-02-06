package org.example.task.dtos

import org.example.task.TaskPriority
import java.util.Date

data class TaskCreateRequest(
    val boardId: Long,
    val stateId: Long,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority? = null,
    var estimatedHours: Double? = null,
    var deadline: Date? = null,
    var orderIndex: Int? = null,
    var tags: List<String>? = null,
)