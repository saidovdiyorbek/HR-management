package org.example.organization.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SetCurrentOrganizationRequest(
    @field:Positive(message = "organizationId must be greater than 0")
    val organizationId: Long
)

data class CurrentOrganizationResponse(
    val organizationId: Long,
    val employeeId: Long,
    val userId: Long,
)