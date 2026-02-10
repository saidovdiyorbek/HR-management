package org.example.project.dtos

import java.time.LocalDateTime

data class BoardCreateDto(
    val name: String,
    val description: String,
    val projectId: Long,
    val states: List<BoardTaskStateDefinitionDto>?,
    val templateId: Long?
)

data class BoardUpdateDto(
    val name: String?,
    val description: String?,
    val projectId: Long?
)

data class BoardShortResponseDto(
    val id: Long,
    val name: String,
)
data class BoardFullResponseDto(
    val id: Long,
    val name: String,
    val description: String,
    val assignedUsers: List<Long>,
    val taskStates: List<TaskStateShortResponseDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BoardTaskStateDefinitionDto(
    val stateId: Long,
    val position: Int
)
data class AssignUsersToBoardDto(
    val userIds: List<Long>
)
