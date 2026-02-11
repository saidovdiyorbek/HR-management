package org.example.project.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.example.project.Permission

data class TaskStateCreateDto(
    @field:NotBlank(message = "{taskstate.name.notblank}")
    @field:Size(min = 1, max = 100, message = "{taskstate.name.size}")
    val name: String,

    @field:NotBlank(message = "{taskstate.description.notblank}")
    @field:Size(max = 500, message = "{taskstate.description.size}")
    val description: String,

    @field:NotNull(message = "{taskstate.permission.notnull}")
    val permission: Permission
)

data class TaskStateUpdateDto(
    @field:Size(min = 1, max = 100, message = "{taskstate.name.size}")
    val name: String?,

    @field:Size(max = 500, message = "{taskstate.description.size}")
    val description: String?,

    val permission: Permission?
)

data class TaskStateShortResponseDto(
    val id: Long,
    val name: String
)

data class TaskStateFullResponseDto(
    val id: Long,
    val name: String,
    val description: String,
    val permission: Permission,
    val companyId: Long
)

data class TaskStateWithPositionDto(
    val id: Long,
    val permission: Permission,
    val position: Int
)

data class TaskStateTemplateCreateDto(
    @field:NotBlank(message = "{taskstate.template.name.notblank}")
    @field:Size(min = 1, max = 100, message = "{taskstate.template.name.size}")
    val name: String,

    @field:NotEmpty(message = "{taskstate.template.states.notempty}")
    @field:Valid
    val states: List<TaskStateTemplateItemDto>
)

data class TaskStateTemplateItemDto(
    @field:NotNull(message = "{taskstate.template.item.id.notnull}")
    @field:Positive(message = "{taskstate.template.item.id.positive}")
    val taskStateId: Long,

    @field:NotNull(message = "{taskstate.template.item.position.notnull}")
    @field:Positive(message = "{taskstate.template.item.position.positive}")
    val position: Int
)

data class TaskStateTemplateResponseDto(
    val id: Long,
    val name: String,
    val states: List<TaskStateTemplateItemResponseDto>
)

data class TaskStateTemplateItemResponseDto(
    val id: Long,
    val taskState: TaskStateShortResponseDto,
    val position: Int
)
