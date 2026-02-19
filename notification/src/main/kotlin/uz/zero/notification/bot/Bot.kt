package uz.zero.notification.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import uz.zero.notification.UserTelegramService

@Component
class Bot(
    private val botProperties: BotProperties,
    private val userTelegramService: UserTelegramService,
) : TelegramLongPollingBot(botProperties.token) {

    override fun onUpdateReceived(update: Update?) {
        val message = update?.message ?: return
        val text = message.text ?: return
        val from = message.from ?: return

        val parts = text.trim().split(" ")

        if (parts.size > 1 && parts[0] == "/start") {
            val hash = parts[1].trim()
            userTelegramService.createOrUpdate(hash, from)
        }
    }

    override fun getBotUsername(): String = botProperties.username
}
