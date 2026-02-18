package uz.zero.notification.bot

import java.time.LocalDateTime

class BotMessage(

) {
    fun create( date: LocalDateTime,
               organizationName: String,
               projectName: String,
               actionOwner: String,
               title: String,
               taskUrl: String): String {
        return """
            📋 <b>Topshiriq yaratildi:</b>
            
            🕐 $date
            🏢 Tashkilot nomi: $organizationName
            📁 Loyiha nomi: $projectName
            🧑‍💼 Harakat egasi: $actionOwner
            📝 Sarlavha: $title
            📊 Holat: Task yaratildi
            🔗 <a href="$taskUrl">Topshiriqni ochish</a>
        """.trimIndent()
    }

    fun changeTaskState(date: LocalDateTime,
                        organizationName: String,
                        projectName: String,
                        actionOwner: String,
                        title: String,
                        fromState: String,
                        toState: String,
                        taskUrl: String) : String {

        return """
            📋 <b>Topshiriqning holati o'zgartirildi:</b>
            
            🕐 $date
            🏢 Tashkilot nomi: $organizationName
            📁 Loyiha nomi: $projectName
            🧑‍💼 Harakat egasi: $actionOwner
            📝 Sarlavha: $title
            📊 Holat: $fromState >> $toState
            🔗 <a href="$taskUrl">Topshiriqni ochish</a>
        """.trimIndent()
    }

    fun changeTaskTitle(date: LocalDateTime,
                        organizationName: String,
                        projectName: String,
                        actionOwner: String,
                        fromTitle: String,
                        toTitle: String,
                        state: String,
                        taskUrl: String) : String {

        return """
            📋 <b>Topshiriqning sarlavhasi o'zgartirildi:</b>
            
            🕐 $date
            🏢 Tashkilot nomi: $organizationName
            📁 Loyiha nomi: $projectName
            🧑‍💼 Harakat egasi: $actionOwner
            📝 Sarlavha: $fromTitle >>> $toTitle
            📊 Holat: $state
            🔗 <a href="$taskUrl">Topshiriqni ochish</a>
        """.trimIndent()
    }
}