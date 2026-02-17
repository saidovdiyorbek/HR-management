package uz.zero.notification.dtos

import uz.zero.notification.ActionType

class TaskActionEvent(
    val taskId: Long,
    val userId: Long,
    val actionType: ActionType,
    val details: String? = null,
)