package uz.zero.auth.exceptions
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uz.davrbank.auth.models.responses.BaseMessage
import uz.zero.auth.enums.ErrorCode
import java.util.Locale

//
//import org.springframework.context.i18n.LocaleContextHolder
//import org.springframework.context.support.ResourceBundleMessageSource
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.MethodArgumentNotValidException
//import org.springframework.web.bind.annotation.ControllerAdvice
//import org.springframework.web.bind.annotation.ExceptionHandler
//import uz.davrbank.auth.models.responses.BaseMessage
//import uz.zero.auth.enums.ErrorCode
//import java.util.stream.Collectors
//
//@ControllerAdvice
//class ExceptionControllerAdvice(
//    private val source: ResourceBundleMessageSource
//) {
//    @ExceptionHandler(DavrException::class)
//    fun handleDBusinessException(exception: DavrException): ResponseEntity<BaseMessage> {
//        return when (exception) {
////            is FeignErrorException -> ResponseEntity.badRequest()
////                .body(BaseMessage(exception.code, exception.errorMessage))
//
//            else -> ResponseEntity.badRequest().body(exception.getErrorMessage(source))
//        }
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException::class)
//    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<BaseMessage> {
//        val fieldErrorList: MutableList<BaseMessage.ValidationFieldError> = ex.bindingResult.fieldErrors.stream()
//            .map { error ->
//                BaseMessage.ValidationFieldError(
//                    error.field,
//                    error.defaultMessage ?: "Validation error"
//                )
//            }
//            .collect(Collectors.toList())
//        return ResponseEntity(
//            BaseMessage(
//                ErrorCode.VALIDATION_ERROR.code,
//                getErrorMessage(ErrorCode.VALIDATION_ERROR.name, null, source),
//                fieldErrorList
//            ),
//            HttpStatus.BAD_REQUEST
//        )
//    }
//
//    fun getErrorMessage(
//        errorCode: String,
//        errorMessageArguments: Array<Any?>?,
//        errorMessageSource: ResourceBundleMessageSource
//    ): String? {
//        val errorMessage = try {
//            errorMessageSource.getMessage(errorCode, errorMessageArguments, LocaleContextHolder.getLocale())
//        } catch (e: Exception) {
//            e.message
//        }
//        return errorMessage
//    }
//}

@RestControllerAdvice
class TaskExceptionHandler(
    private val messageSource: MessageSource
) {

    @ExceptionHandler(AuthAppException::class)
    fun handleTaskAppException(ex: AuthAppException): ResponseEntity<BaseMessage> {
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<BaseMessage> {
        val filedError: FieldError = ex.bindingResult.allErrors.first() as FieldError

        val local = LocaleContextHolder.getLocale()
        val errorMessage = filedError.defaultMessage ?: "Validation error"

        val message = try {
            messageSource.getMessage(errorMessage, null, local)
        }catch (e: NoSuchMessageException) {
            errorMessage.replace("_", " ").lowercase()
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseMessage(
                code = 400,
                message = "${filedError.field}: $message"
            ))
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

sealed class AuthAppException(message: String? = null) : RuntimeException() {
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

class UserNotFoundException(): AuthAppException(){
    override fun errorType()= ErrorCode.USER_NOT_FOUND
}

class UsernameAlreadyExistsException(): AuthAppException() {
    override fun errorType() = ErrorCode.USERNAME_ALREADY_EXISTS
}

class UserIsAdminException(): AuthAppException() {
    override fun errorType() = ErrorCode.USER_IS_ADMIN
}