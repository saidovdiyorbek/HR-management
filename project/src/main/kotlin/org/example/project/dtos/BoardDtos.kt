package org.example.project.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class BoardCreateDto(
    @field:NotBlank(message = "{board.name.notblank}")
    @field:Size(min = 1, max = 100, message = "{board.name.size}")
    val name: String,

    @field:NotBlank(message = "{board.description.notblank}")
    @field:Size(max = 1000, message = "{board.description.size}")
    val description: String,

    @field:NotNull(message = "{board.projectid.notnull}")
    @field:Positive(message = "{board.projectid.positive}")
    val projectId: Long,

    @field:Valid
    val states: List<BoardTaskStateDefinitionDto>?,

    @field:Positive(message = "{board.templateid.positive}")
    val templateId: Long?
)

data class BoardUpdateDto(
    @field:Size(min = 1, max = 100, message = "{board.name.size}")
    val name: String?,

    @field:Size(max = 1000, message = "{board.description.size}")
    val description: String?,

    @field:Positive(message = "{board.projectid.positive}")
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
    @field:NotNull(message = "{board.state.id.notnull}")
    @field:Positive(message = "{board.state.id.positive}")
    val stateId: Long,

    @field:NotNull(message = "{board.state.position.notnull}")
    @field:Positive(message = "{board.state.position.positive}")
    val position: Int
)


data class AssignUsersToBoardDto(
    @field:NotEmpty(message = "{board.users.notempty}")
    val userIds: List<@Positive(message = "{board.userid.positive}") Long>
)

data class StatePositionUpdateDto(
    @field:NotNull(message = "{board.state.id.notnull}")
    @field:Positive(message = "{board.state.id.positive}")
    val stateId: Long,

    @field:NotNull(message = "{board.state.newposition.notnull}")
    @field:Positive(message = "{board.state.newposition.positive}")
    val newPosition: Int
)
