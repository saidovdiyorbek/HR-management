package org.example.task
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.stereotype.Component
import java.util.Date

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
    val isUpdate: Boolean = false,
)
data class InternalHashesCheckRequest(
    val userId: Long,
    val hashes: List<String>
)

data class  TransferTaskCheckDto(
    val fromStateId: Long,
    val toStateId: Long,
    val boardId: Long,
    val permission: Permission,
)

data class EmployeeRoleResponse(
    val employeeRole: EmployeeRole
)

data class CheckUsersInOrganizationRequest(
    val organizationId: Long,
    val userIds: List<Long>
)

data class CurrentOrganizationResponse(
    val organizationId: Long,
    val employeeId: Long,
    val userId: Long,
)

data class StateShortInfoDto(
    val id :Long,
    val name: String,
    val order: Int
)

data class BoardInfoDto(
    val id: Long,
    val name: String,
    val states: List<StateShortInfoDto>
)

data class CheckResponse(
    val organizationId: Long
)

data class RequestEmployeeRole(

    @field:NotNull(message = "userId is required")
    @field:Positive(message = "userId must be greater than 0")
    val userId: Long,

    @field:NotNull(message = "organizationId is required")
    @field:Positive(message = "organizationId must be greater than 0")
    val organizationId: Long
)

data class TaskActionCreateDto(
    val taskId: Long,
    val userId: Long,
    val type: ActionType,
    val details: String? = null,
)



data class TaskEventDto(
    var task: TaskShortInfoDto,
    var userId: Long,
    var action: ActionType,
    var actionDetails: ActionDetails? = null
)

data class TaskShortInfoDto(
var taskId: Long? = null,
var boardId: Long,
var title: String,
var assignedEmployeesIds: List<Long>? = null,
)

data class ActionDetails(
var fromState: Long? = null,
var toState: Long? = null,
var title: String? = null,
var attachesHashes: List<String>? = null,
var addedEmployeeIds: List<Long>? = null,
)


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
    val boarInfo: BoardInfoDto,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority? = null,
    var estimatedHours: Double? = null,
    var deadline: Date? = null,
    var tags: List<String>? = null,
    var attachHashes: List<String>? = null,
)

data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)

data class TaskHistoryResponse(
    val id: Long,
    val changedByEmployeeId: Long,
    val actionType: ActionType,
    val details: TaskHistoryDetails? = null,
)

data class TaskHistoryDetails(
    var fromStateId: Long? = null,
    var toStateId: Long? = null,
    var oldTitle: String? = null,
    var newTitle: String? = null,
    var addedAttaches: List<String>? = null,
    var assignedEmployees: List<Long>? = null,
)