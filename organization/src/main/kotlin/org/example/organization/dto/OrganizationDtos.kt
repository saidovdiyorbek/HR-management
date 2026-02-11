package org.example.organization.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class OrganizationCreateRequest(

    @field:NotBlank(message = "Organization name cannot be blank")
    @field:Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    val isActive: Boolean = true
)

data class OrganizationUpdateRequest(

    @field:Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    val name: String? = null,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
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
