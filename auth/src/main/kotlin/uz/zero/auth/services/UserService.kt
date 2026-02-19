package uz.zero.auth.services

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.zero.auth.dtos.UserShortInfo
import uz.zero.auth.enums.Role
import uz.zero.auth.exceptions.UserNotFoundException
import uz.zero.auth.exceptions.UsernameAlreadyExistsException
import uz.zero.auth.mappers.UserEntityMapper
import uz.zero.auth.model.requests.PasswordUpdateDto
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.requests.UserUpdateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.repositories.UserRepository
import uz.zero.auth.utils.userId

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserEntityMapper,
    private val passwordEncoder: PasswordEncoder,
) {
    fun userMe(): UserInfoResponse {
        return userMapper
            .toUserInfo(userRepository.findByIdAndDeletedFalse(userId())!!)
    }

    @Transactional
    fun registerUser(request: UserCreateRequest) {
        if (userRepository.existsByUsername(request.username))
            throw UsernameAlreadyExistsException()

        userRepository.save(userMapper.toEntity(request, Role.USER))
    }

    fun checkUserExist(userId: Long): Role {
        val res = userRepository.findByIdAndDeletedFalse(userId)?:throw UserNotFoundException()
        return res.role
    }

    fun update(id: Long, request: UserUpdateRequest) {
        val user = userRepository.findByIdAndDeletedFalse(userId())
            ?: throw UserNotFoundException()

        request.username?.let {
            if (user.username != request.username && userRepository.existsByUsername(request.username))
                throw UsernameAlreadyExistsException()
            user.username = it
        }

        request.fullName?.let {
            user.fullName = it
        }

        userRepository.save(user)
    }

    fun updatePassword(id: Long, newPassword: PasswordUpdateDto) {
        val user = userRepository.findByIdAndDeletedFalse(userId())
            ?: throw UserNotFoundException()

        user.password = passwordEncoder.encode(newPassword.password)

        userRepository.save(user)
    }

    fun getAll(): List<UserInfoResponse> {
        return userRepository.findAllNotDeleted().map { it -> userMapper.toUserInfo(it) }
    }

    fun getUserShortInfo(userId: Long): UserShortInfo {
        userRepository.findByIdAndDeletedFalse(userId)?.let { user ->
            return UserShortInfo(
                user.id!!,
                user.username,
                user.fullName,
            )
        }
        throw UserNotFoundException()
    }

}