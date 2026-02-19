package uz.zero.notification.bot

import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BotMessage {


    fun buildMessage(
        date: LocalDateTime,
        organizationName: String,
        projectName: String,
        actionOwner: String,
        title: String?,
        taskUrl: String,
        lines: List<String>,
    ): String {
        val statusLines = lines.mapIndexed { index, line ->
            if (lines.size > 1) "📊 Holat ${index + 1}: $line"
            else "📊 Holat: $line"
        }.joinToString("\n")

        return """
            📋 <b>Topshiriq yangilandi:</b>
            
            🕐 $date
            🏢 Tashkilot: $organizationName
            📁 Loyiha: $projectName
            🧑‍💼 Harakat egasi: $actionOwner
            📝 Sarlavha: $title
            $statusLines
            🔗 <a href="$taskUrl">Topshiriqni ochish</a>
        """.trimIndent()
    }
}