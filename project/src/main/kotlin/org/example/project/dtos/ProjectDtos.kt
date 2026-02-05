package org.example.project.dtos

import jakarta.validation.constraints.NotBlank

data class ProjectCreateDto(
    val name: String,
    val description: String
)

data class ProjectShortResponseDto(
    val id: Long,
    val name: String
)

data class ProjectFullResponseDto(
    val id: Long,
    val name: String,
    val description: String,
    val organizationId: Long,
    val boards : List<BoardShortResponseDto>,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class ProjectUpdateDto(
    val name: String?,
    val description: String?,
    val organizationId: Long?,
)
