package org.example.project

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.example.project.dtos.BaseMessage
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.util.Locale

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

class FeignClientException(
    val errorCode: Int?,
    val errorMessage: String?
) : RuntimeException(errorMessage) {
    fun toBaseMessage(): BaseMessage {
        return BaseMessage(code = errorCode, message = errorMessage)
    }
}

@RestControllerAdvice
class ExceptionHandler(
    private val errorMessageSource: ResourceBundleMessageSource
) {

    @ExceptionHandler(ProjectAppException::class)
    fun handleOtherExceptions(exception: Throwable): ResponseEntity<BaseMessage> {
        when (exception) {
            is ProjectAppException-> {
                val locale = LocaleContextHolder.getLocale()
                val message = try {
                    errorMessageSource.getMessage(exception.errorType().toString(), null, locale)
                } catch (e: NoSuchMessageException) {
                    exception.message ?: exception.errorType().toString().replace("_", " ").lowercase()
                }

                return ResponseEntity
                    .badRequest()
                    .body(BaseMessage(exception.errorType().code, message))
            }

            else -> {
                exception.printStackTrace()
                return ResponseEntity
                    .badRequest().body(
                        BaseMessage(100,
                            "Iltimos support bilan bog'laning")
                    )
            }
        }
    }

    @ExceptionHandler(FeignClientException::class)
    fun handleFeignClientException(ex: FeignClientException): ResponseEntity<BaseMessage> {
        return ResponseEntity
            .badRequest()
            .body(ex.toBaseMessage())
    }

}



sealed class ProjectAppException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null
    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments() as Array<out Any>?,
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}

class ProjectNotFoundException() : ProjectAppException() {
    override fun errorType(): ErrorCode = ErrorCode.PROJECT_NOT_FOUND
}

class BoardNotFoundException() : ProjectAppException() {
    override fun errorType(): ErrorCode = ErrorCode.BOARD_NOT_FOUND
}

class TaskStateNotFoundException() : ProjectAppException() {
    override fun errorType(): ErrorCode = ErrorCode.TASK_STATE_NOT_FOUND
}

class BoardTaskStateNotFoundException() : ProjectAppException() {
    override fun errorType(): ErrorCode = ErrorCode.BOARD_TASK_STATE_NOT_FOUND
}

class ProjectIsNotActiveException() : ProjectAppException() {
    override fun errorType(): ErrorCode = ErrorCode.PROJECT_IS_NOT_ACTIVE
}
class ProjectEndException() : ProjectAppException() {
    override fun errorType(): ErrorCode = ErrorCode.PROJECT_ENDED
}

class StateIsNotFirstException(): ProjectAppException(){
    override fun errorType(): ErrorCode = ErrorCode.STATE_IS_NOT_FIRST
}

class StateNotConnnectedToBoardException(): ProjectAppException(){
    override fun errorType(): ErrorCode = ErrorCode.STATE_NOT_CONNNECTED_TO_BOARD
}
class NotPermitedToTransferTaskException(): ProjectAppException(){
    override fun errorType(): ErrorCode = ErrorCode.NOT_PERMITED_TO_TRANSFER_TASK
}
class OrdersOfStatesIsIncorrectException(): ProjectAppException(){
    override fun errorType(): ErrorCode = ErrorCode.ORDERS_OF_STATES_IS_INCORRECT
}


