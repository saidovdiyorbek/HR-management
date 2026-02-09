package org.example.project.dtos

import org.example.project.Permission

data class TaskStateCreateDto(
    val boardId: Long?,
    val name: String,
    val description: String,
    val permission: Permission
)

data class TaskStateUpdateDto(
    val name: String?,
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
    val name: String,
    val states: List<TaskStateTemplateItemDto>
)

data class TaskStateTemplateItemDto(
    val taskStateId: Long,
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
