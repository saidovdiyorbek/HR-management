package uz.zero.auth.dtos

data class UserShortInfo(
    val userId: Long,
    val username: String,
    val fullName: String?,
)