package uz.zero.notification.dtos

import uz.zero.notification.ActionType

data class UserShortInfo(
    val userId: Long,
    val telegramChatId: Long? = null,
    val username: String,
)

data class TaskActionCreateDto(
    val taskId: Long,
    val userId: Long,
    val type: ActionType,
    val details: String? = null,
)