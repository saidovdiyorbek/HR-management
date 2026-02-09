package org.example.task.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.example.task.Role
import org.example.task.TaskPriority
import org.example.task.ValidEnum
import java.util.Date

data class TaskCreateRequest(
    @field:Size(min = 1)
    val boardId: Long,
    @field:Size(min = 1)
    val stateId: Long,
    @field:NotBlank
    @field:Size(max = 255)
    val title: String,
    val description: String? = null,
    @field:ValidEnum(enumClass = Role::class, message = "Role not found")
    val priority: TaskPriority? = null,
    var estimatedHours: Double? = null,
    var deadline: Date? = null,
    var tags: List<String>? = null,
    var attachHashes: List<String>? = null,
    var assigningEmployeesId: List<Long>? = null,
)

data class TaskUpdateRequest(
    @field:Size(min = 1)
    val stateId: Long? = null,
    @field:NotBlank
    @field:Size(max = 255)
    val title: String? = null,
    val description: String? = null,
    @field:ValidEnum(enumClass = Role::class, message = "Role not found")
    val priority: TaskPriority? = null,
    var estimatedHours: Double? = null,
    var deadline: Date? = null,
    var tags: List<String>? = null,
    var attachHashes: List<String>? = null,
)

data class TaskResponse(
    val id: Long,
    val boardId: Long,
    val stateId: Long,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority? = null,
    var estimatedHours: Double? = null,
    var deadline: Date? = null,
    var tags: List<String>? = null,
    var attachHashes: List<String>? = null,
)