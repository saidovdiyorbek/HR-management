package uz.zero.notification.dtos

import uz.zero.notification.ActionType


data class TaskEventDto(
    val task: TaskShortInfoDto,
    val userId: Long,
    val action: ActionType,
)

data class TaskShortInfoDto(
    val taskId: Long,
    val boardId: Long,
    val title: String,
)