package org.example.organization.dto

data class BaseMessage(
    val code: Long? = null,
    val message: String? = null
)

data class OrganizationCreateRequest(
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true
)

data class OrganizationUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)

data class OrganizationAllResponse(
    val id: Long,
    val name: String,
    val isActive: Boolean
)

data class OrganizationFullResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean
)
