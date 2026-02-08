package org.example.organization

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.example.organization.dto.BaseMessage
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
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

@ControllerAdvice
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) {
    @ExceptionHandler(OrganizationAppException::class)
    fun organizationAppException(ex: OrganizationAppException): ResponseEntity<BaseMessage> {
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
}


sealed class OrganizationAppException(message: String? = null) : RuntimeException() {
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

class FeignClientException(
    val errorCode: Int?,
    val errorMessage: String?
) : RuntimeException(errorMessage) {
    fun toBaseMessage(): BaseMessage {
        return BaseMessage(code = errorCode, message = errorMessage)
    }
}

class OrganizationAlreadyExistsException : OrganizationAppException() {
    override fun errorType() = ErrorCode.ORGANIZATION_ALREADY_EXISTS
}

class OrganizationNotFoundException : OrganizationAppException() {
    override fun errorType() = ErrorCode.ORGANIZATION_NOT_FOUND
}

class OrganizationNotActiveException : OrganizationAppException() {
    override fun errorType() = ErrorCode.ORGANIZATION_NOT_ACTIVE
}

class EmployeeAlreadyExistsException : OrganizationAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_ALREADY_EXISTS
}

class EmployeeNotFoundException : OrganizationAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_NOT_FOUND
}

class EmployeeNotInOrganizationException : OrganizationAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_NOT_IN_ORGANIZATION
}

class EmployeeContextNotFoundException : OrganizationAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_CONTEX_NOT_FOUND
}