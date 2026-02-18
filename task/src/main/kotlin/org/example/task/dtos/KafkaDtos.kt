package org.example.task.dtos

import org.example.task.ActionType

data class TaskEventDto(
    val task: TaskShortInfoDto,
    val userId: Long,
    val action: ActionType,
    val actionDetails: ActionDetails? = null
)

data class TaskShortInfoDto(
    val taskId: Long? = null,
    val boardId: Long,
    val title: String,
    val assignedEmployeesIds: List<Long>,
)

data class ActionDetails(
    val fromState: Long? = null,
    val toState: Long? = null,
    val title: String? = null,
)