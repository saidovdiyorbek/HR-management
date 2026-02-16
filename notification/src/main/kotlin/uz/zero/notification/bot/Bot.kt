package uz.zero.notification.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class Bot (
    private val botProperties: BotProperties,
): TelegramLongPollingBot(botProperties.token) {
    override fun onUpdateReceived(update: Update?) {
        TODO("Not yet implemented")
    }

    override fun getBotUsername(): String? {
        TODO("Not yet implemented")
    }
}

