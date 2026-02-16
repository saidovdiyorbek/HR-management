package uz.zero.notification.bot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@ConfigurationProperties(prefix = "telegram.bot")
data class BotProperties(
    val token: String,
    val username: String
)

@Configuration
class BotConfiguration {

    @Bean
    fun telegramBotsApi(bot: Bot): TelegramBotsApi {
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        api.registerBot(bot)
        return api
    }
}