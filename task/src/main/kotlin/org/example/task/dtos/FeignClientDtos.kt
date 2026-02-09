package org.example.task.dtos

import org.example.task.Permission

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
)
data class InternalHashesCheckRequest(
    val userId: Long,
    val hashes: List<String>
)

data class TransferTaskCheckDto(
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
    val organizationId: Long
)
