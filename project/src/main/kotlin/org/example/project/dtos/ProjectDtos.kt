package org.example.project.dtos

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

data class ProjectCreateDto(
    val name: String,
    val description: String?
)

data class ProjectShortResponseDto(
    val id: Long,
    val name: String
)

data class ProjectFullResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val organizationId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val boards : List<BoardShortResponseDto>,
    val isActive: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class ProjectUpdateDto(
    val name: String?,
    val description: String?,
    val organizationId: Long?,
)
