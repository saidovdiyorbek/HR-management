package uz.zero.notification.dtos
import com.fasterxml.jackson.annotation.JsonProperty
import uz.zero.notification.ActionType


data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}


data class UserShortInfo(
    val userId: Long,
    val username: String,
    val fullName: String,
)

data class TaskActionCreateDto(
    val taskId: Long,
    val userId: Long,
    val type: ActionType,
    val details: String? = null,
)

data class CurrentOrganizationResponse(
    val organizationId: Long,
    val employeeId: Long,
    val userId: Long,
)

data class OrganizationInfo(
    val id: Long,
    val name: String,
    val description: String? = null,
)
data class ProjectShortInfo(
    val projectId: Long?,
    val projectName: String,
    val board: BoardInfoDto,
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

class TaskActionEvent(
    val taskId: Long,
    val userId: Long,
    val actionType: ActionType,
    val details: String? = null,
)



data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)