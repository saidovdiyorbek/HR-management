package uz.zero.auth.dtos

data class UserShortInfo(
    val userId: Long,
    val telegramChatId: Long? = null,
    val username: String,
    val fullName: String?,
)