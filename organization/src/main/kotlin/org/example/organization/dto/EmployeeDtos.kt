package org.example.organization.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.example.organization.EmployeeRole
import java.time.LocalDateTime

data class EmployeeCreateRequest(

    @field:NotNull(message = "userId is required")
    @field:Positive(message = "userId must be greater than 0")
    val userId: Long,

    @field:NotNull(message = "employeeRole is required")
    val employeeRole: EmployeeRole,

    @field:Size(min = 2, max = 100, message = "Position must be between 2 and 100 characters")
    val position: String? = null,

    @field:Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    val department: String? = null
)

data class EmployeeUpdateRequest(

    @field:Size(min = 2, max = 100, message = "Position must be between 2 and 100 characters")
    val position: String? = null,

    @field:Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
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
    @field:NotNull(message = "employeeRole is required")
    val employeeRole: EmployeeRole
)

data class RequestEmployeeRole(

    @field:NotNull(message = "userId is required")
    @field:Positive(message = "userId must be greater than 0")
    val userId: Long,

    @field:NotNull(message = "organizationId is required")
    @field:Positive(message = "organizationId must be greater than 0")
    val organizationId: Long
)

data class CheckUsersInOrganizationRequest(
    val organizationId: Long,
    val userIds: List<Long>
)

data class CheckUsersInOrganizationResponse(
    val result: Boolean
)

data class AllEmployeesResponse(
    val id: Long,
    val userId: Long,
    val organizationId: List<Long>,
    val employeeRole: List<EmployeeRole>,
)
