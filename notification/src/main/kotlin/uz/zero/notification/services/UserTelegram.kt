package uz.zero.notification.services

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.User
import uz.zero.notification.UserTelegram
import uz.zero.notification.UserTelegramRepository

interface UserTelegramService {
    fun createOrUpdate(hash: String, from: User)
}

@Service
class UserTelegramImpl(
    val repository: UserTelegramRepository,
    val hashService: HashService,
) : UserTelegramService {
    override fun createOrUpdate(hash: String, from: User) {
        val userId= hashService.checkHashAndReturnUserId(hash)
        var user = repository.findByUserIdAndDeletedIsFalse(userId)

        if(user == null) {
            user = UserTelegram(
                chatId = from.id,
                firstName = from.firstName,
                lastName = from.lastName,
                username = from.userName,
                userId = userId
            )
        }else{
            user.chatId = from.id
            user.firstName = from.firstName
            user.lastName = from.lastName
            user.username = from.userName
        }

        repository.save(user)
    }
}