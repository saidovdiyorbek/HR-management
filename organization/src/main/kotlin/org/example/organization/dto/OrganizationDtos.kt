package org.example.organization.dto

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

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
