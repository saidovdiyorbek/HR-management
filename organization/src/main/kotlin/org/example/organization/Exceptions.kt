package org.example.organization

import org.example.organization.dto.BaseMessage
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) {
    @ExceptionHandler(DemoException::class)
    fun handleDemoException(exception: DemoException): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest()
            .body(exception.getErrorMessage(messageSource))
    }
}


sealed class DemoException : RuntimeException() {
    abstract fun errorCode(): ErrorCode
    open fun getArguments(): Array<Any?>? = null

    fun getErrorMessage(messageSource: MessageSource): BaseMessage {
        val code = errorCode()
        val message = try {
            messageSource.getMessage(
                code.name,              // messages.properties dagi key
                getArguments(),
                LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message ?: code.name
        }

        return BaseMessage(code.code, message)
    }
}

class OrganizationAlreadyExistsException : DemoException() {
    override fun errorCode() = ErrorCode.ORGANIZATION_ALREADY_EXISTS
}

class OrganizationNotFoundException : DemoException() {
    override fun errorCode() = ErrorCode.ORGANIZATION_NOT_FOUND
}

class OrganizationNotActiveException : DemoException() {
    override fun errorCode() = ErrorCode.ORGANIZATION_NOT_ACTIVE
}

class EmployeeAlreadyExistsException : DemoException() {
    override fun errorCode() = ErrorCode.EMPLOYEE_ALREADY_EXISTS
}

class EmployeeNotFoundException : DemoException() {
    override fun errorCode() = ErrorCode.EMPLOYEE_NOT_FOUND
}

class EmployeeNotInOrganizationException : DemoException() {
    override fun errorCode() = ErrorCode.EMPLOYEE_NOT_IN_ORGANIZATION
}

class EmployeeContextNotFoundException : DemoException() {
    override fun errorCode() = ErrorCode.EMPLOYEE_CONTEX_NOT_FOUND
}