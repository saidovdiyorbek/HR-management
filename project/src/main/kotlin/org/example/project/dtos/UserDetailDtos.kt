package org.example.project.dtos

data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)