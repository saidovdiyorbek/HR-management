package uz.zero.auth.model.requests

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import uz.zero.auth.utils.NotSpace
import kotlin.arrayOf

data class UserUpdateRequest (
    @field:Size(min = 3, max = 32)
    @field:NotSpace
    val username: String? = null,
    @field:Size(max = 255) val fullName: String? = null,
)