package uz.zero.notification.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SendMessageRequest(
    @JsonProperty("chat_id") val chatId: Long,
    @JsonProperty("text") val text: String,
    @JsonProperty("reply_markup") val replyMarkup: Any? = null,
    @JsonProperty("parse_mode") val parseMode: String? = "HTML",
    @JsonProperty("disable_web_page_preview") val disableWebPagePreview: Boolean = false,
    @JsonProperty("disable_notification") val disableNotification: Boolean = false,
    @JsonProperty("reply_to_message_id") val replyToMessageId: Long? = null,


    )
