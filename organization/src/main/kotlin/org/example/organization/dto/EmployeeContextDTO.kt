package org.example.organization.dto

data class SetCurrentOrganizationRequest(
    val organizationId: Long
)

data class CurrentOrganizationResponse(
    val organizationId: Long
)