package uz.zero.notification.services

import org.hibernate.validator.constraints.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uz.zero.notification.Hash
import uz.zero.notification.HashNotValidException
import uz.zero.notification.HashRepository
import uz.zero.notification.SecurityUtil
import uz.zero.notification.UserTelegram
import uz.zero.notification.bot.BotProperties

interface HashService {
    fun generateHash(): String
    fun checkHashAndReturnUserId(hash: String): Long
    fun removeHash(hash: String)
}

@Service
class HashServiceImpl(
    val securityUtil: SecurityUtil,
    val repository: HashRepository,
    @Value("\${hash.expiration}") val expirationTime: Long,
    @Value("\${hash.telegram.url}") val telegramUrl: String,
    val botProperties: BotProperties
) : HashService {
    override fun generateHash(): String {
        val userId = securityUtil.getCurrentUserId()
        val hash = java.util.UUID.randomUUID().toString()
        val hashEntity = Hash(
            userId = userId,
            hash = hash,
            expriTime = java.time.LocalDateTime.now().plusMinutes(expirationTime),
            url = telegramUrl.plus("${botProperties.username}?start=$hash"),
        )
        repository.save(hashEntity)
        return hashEntity.url
    }

    override fun checkHashAndReturnUserId(hash: String): Long {
        val entity = repository.findByHashAndDeletedFalse(hash)
        if (entity == null || entity.expriTime.isBefore(java.time.LocalDateTime.now())) {
            throw HashNotValidException()
        }
        return entity.userId
    }

    override fun removeHash(hash: String) {
        repository.deleteByHash(hash)
    }

}