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
        headerText: String = "📋 <b>Topshiriq yangilandi:</b>"
    ): String {
        val statusLines = lines.mapIndexed { index, line ->
            if (lines.size > 1) "📊 Holat ${index + 1}: $line"
            else "📊 Holat: $line"
        }.joinToString("\n")

        return """
            $headerText
            
            🕐 $date
            🏢 Tashkilot: ${organizationName.escapeHtml()}
            📁 Loyiha: ${projectName.escapeHtml()}
            🧑‍💼 Harakat egasi: ${actionOwner.escapeHtml()}
            📝 Sarlavha: ${title?.escapeHtml() ?: "-"}
            $statusLines
            🔗 <a href="$taskUrl">Topshiriqni ochish</a>
        """.trimIndent()
    }

    private fun String.escapeHtml(): String {
        return this.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}