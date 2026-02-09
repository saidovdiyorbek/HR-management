package org.example.organization.dto

import org.example.organization.EmployeeRole
import java.time.LocalDateTime

data class EmployeeCreateRequest(
    val userId: Long,
    val employeeRole: EmployeeRole,
    val position: String? = null,
    val department: String? = null
)

data class EmployeeUpdateRequest(
    val position: String? = null,
    val department: String? = null,
    val isActive: Boolean? = null
)

data class EmployeeResponse(
    val id: Long,
    val userId: Long,
    val organizationId: Long,
    val employeeRole: EmployeeRole,
    val position: String?,
    val department: String?,
    val isActive: Boolean,
    val joinedAt: LocalDateTime
)

data class EmployeeRoleResponse(
    val employeeRole: EmployeeRole
)

data class EmployeeRoleUpdateRequest(
    val employeeRole: EmployeeRole
)

data class RequestEmployeeRole(
    val userId: Long,
    val organizationId: Long
)
