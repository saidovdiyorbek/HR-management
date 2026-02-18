package org.example.task.dtos

import org.example.task.ActionType

data class TaskEventDto(
    val task: TaskShortInfoDto,
    val userId: Long,
    val action: ActionType,
)

data class TaskShortInfoDto(
    val taskId: Long? = null,
    val boardId: Long,
    val title: String,
)