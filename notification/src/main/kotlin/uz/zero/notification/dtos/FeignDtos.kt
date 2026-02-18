package uz.zero.notification.dtos

import uz.zero.notification.ActionType

data class UserShortInfo(
    val userId: Long,
    val telegramChatId: Long? = null,
    val username: String,
    val fullName: String,
)

data class TaskActionCreateDto(
    val taskId: Long,
    val userId: Long,
    val type: ActionType,
    val details: String? = null,
)

data class CurrentOrganizationResponse(
    val organizationId: Long,
    val employeeId: Long,
    val userId: Long,
)

data class OrganizationInfo(
    val id: Long,
    val name: String,
    val description: String? = null,
)