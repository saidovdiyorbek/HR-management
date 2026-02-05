package org.example.organization.dto

import org.example.organization.EmployeeRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

data class EmployeeCreateRequest(
    val userId: Long,
    val employeeRole: EmployeeRole,
    val position: String? = null,
    val department: String? = null
)

data class EmployeeUpdateRequest(
    val employeeRole: EmployeeRole? = null,
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