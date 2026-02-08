package org.example.task

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValidator::class])
annotation class ValidEnum(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "Noto'g'ri qiymat kiritildi",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class EnumValidator : ConstraintValidator<ValidEnum, String> {
    private lateinit var enumConstants: List<String>

    override fun initialize(annotation: ValidEnum) {
        enumConstants = annotation.enumClass.java.enumConstants.map { it.name }
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value == null || enumConstants.contains(value)
    }
}