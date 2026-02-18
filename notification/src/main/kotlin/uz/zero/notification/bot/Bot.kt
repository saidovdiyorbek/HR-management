package uz.zero.notification.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import uz.zero.notification.services.UserTelegramService

@Component
class Bot (
    private val botProperties: BotProperties,
    private val userTelegramService: UserTelegramService

    ): TelegramLongPollingBot(botProperties.token) {
    override fun onUpdateReceived(update: Update?) {
        update?.let {
            val message = it.message
            val text = message.text

            val parts = text.split(" ")

            if (parts.size > 1) {
                val hash = parts[1]
                userTelegramService.createOrUpdate(hash, message.from)

            }
        }
    }

    override fun getBotUsername(): String? {
        return botProperties.username
    }
}

