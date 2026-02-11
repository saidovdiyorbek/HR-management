package org.example.project.dtos

import org.example.project.EmployeeRole

import org.example.project.Permission

data class CurrentUserOrganizationDto(
    val organizationId: Long,
)

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
)

data class TransferTaskCheckDto(
    val fromStateId: Long,
    val toStateId: Long,
    val boardId: Long,
    val permission: Permission,
)
data class RequestEmployeeRole(
    val userId: Long,
    val organizationId: Long
)

data class EmployeeRoleResponse(
    val employeeRole: EmployeeRole
)

data class CheckUsersInOrganizationRequest(
    val organizationId: Long,
    val userIds: List<Long>
)

data class BoardUserRequestDto(
    val boardId: Long,
    val userIds: List<Long>
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
