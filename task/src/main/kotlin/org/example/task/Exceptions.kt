package org.example.task

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.example.task.dtos.BaseMessage
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.util.Locale
import kotlin.collections.toString
import kotlin.io.readBytes
import kotlin.jvm.java
import kotlin.text.isNullOrEmpty
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.substringBefore

@Component
class CustomErrorDecoder(
    private val objectMapper: ObjectMapper
) : ErrorDecoder {

    override fun decode(methodKey: String, response: Response): Exception {
        return try {
            val body = response.body()?.asInputStream()?.readBytes()?.toString(Charsets.UTF_8)

            if (body.isNullOrEmpty()) {
                return FeignClientException(
                    errorCode = response.status(),
                    errorMessage = "Unknown error occurred from ${extractServiceName(methodKey)}"
                )
            }

            // BaseMessage ga parse qilish
            val errorResponse = objectMapper.readValue(body, BaseMessage::class.java)

            // FeignClientException ga o'rash
            FeignClientException(
                errorCode = errorResponse.code,
                errorMessage = errorResponse.message ?: "Error from ${extractServiceName(methodKey)}"
            )

        } catch (e: IOException) {
            FeignClientException(
                errorCode = response.status(),
                errorMessage = "Error parsing response from ${extractServiceName(methodKey)}: ${e.message}"
            )
        } catch (e: Exception) {
            FeignClientException(
                errorCode = response.status(),
                errorMessage = "Unexpected error from ${extractServiceName(methodKey)}: ${e.message}"
            )
        }
    }

    // Methoddan service nomini ajratib olish (masalan: "UserFeignClient#getUserById(Long)")
    private fun extractServiceName(methodKey: String): String {
        return methodKey.substringBefore("#")
    }

}

@RestControllerAdvice
class TaskExceptionHandler(
    private val messageSource: MessageSource
) {

    @ExceptionHandler(TaskAppException::class)
    fun handleTaskAppException(ex: TaskAppException): ResponseEntity<BaseMessage> {
        val locale = LocaleContextHolder.getLocale()
        val message = try {
            messageSource.getMessage(ex.errorType().toString(), null, locale)
        } catch (e: NoSuchMessageException) {
            ex.message ?: ex.errorType().toString().replace("_", " ").lowercase()
        }

        return ResponseEntity
            .badRequest()
            .body(BaseMessage(ex.errorType().code, message))
    }

    // Feign orqali kelgan xatolar uchun
    @ExceptionHandler(FeignClientException::class)
    fun handleFeignClientException(ex: FeignClientException): ResponseEntity<BaseMessage> {
        return ResponseEntity
            .badRequest()
            .body(ex.toBaseMessage())
    }
}

class FeignClientException(
    val errorCode: Int?,
    val errorMessage: String?
) : RuntimeException(errorMessage) {
    fun toBaseMessage(): BaseMessage {
        return BaseMessage(code = errorCode, message = errorMessage)
    }
}

sealed class TaskAppException(message: String? = null) : RuntimeException() {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null
    fun gerErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            code = errorType().code,
            message = errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments() as Array<out Any>?,
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}
class TaskNotFoundException() : TaskAppException() {
    override fun errorType() = ErrorCode.TASK_NOT_FOUND
}

