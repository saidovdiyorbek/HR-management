package org.example.task.dtos

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.example.task.ActionType
import org.example.task.EmployeeRole
import org.example.task.Permission

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
    val isUpdate: Boolean = false,
)
data class InternalHashesCheckRequest(
    val userId: Long,
    val hashes: List<String>
)

data class  TransferTaskCheckDto(
    val fromStateId: Long,
    val toStateId: Long,
    val boardId: Long,
    val permission: Permission,
)

data class EmployeeRoleResponse(
    val employeeRole: EmployeeRole
)

data class CheckUsersInOrganizationRequest(
    val organizationId: Long,
    val userIds: List<Long>
)

data class CurrentOrganizationResponse(
    val organizationId: Long,
    val employeeId: Long,
    val userId: Long,
)

data class StateShortInfoDto(
    val id :Long,
    val name: String,
    val order: Int
)

data class BoardInfoDto(
    val id: Long,
    val name: String,
    val states: List<StateShortInfoDto>
)

data class CheckResponse(
    val organizationId: Long
)

data class RequestEmployeeRole(

    @field:NotNull(message = "userId is required")
    @field:Positive(message = "userId must be greater than 0")
    val userId: Long,

    @field:NotNull(message = "organizationId is required")
    @field:Positive(message = "organizationId must be greater than 0")
    val organizationId: Long
)

data class TaskActionCreateDto(
    val taskId: Long,
    val userId: Long,
    val type: ActionType,
    val details: String? = null,
)