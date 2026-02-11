package org.example.project.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date


data class ProjectCreateDto(
    @field:NotBlank(message = "{project.name.notblank}")
    @field:Size(min = 1, max = 100, message = "{project.name.size}")
    val name: String,

    @field:Size(max = 1000, message = "{project.description.size}")
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
    @field:Size(min = 1, max = 100, message = "{project.name.size}")
    val name: String?,

    @field:Size(max = 1000, message = "{project.description.size}")
    val description: String?,

    @field:Positive(message = "{project.organizationid.positive}")
    val organizationId: Long?
)


