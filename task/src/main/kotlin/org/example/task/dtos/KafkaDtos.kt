package org.example.task.dtos

import org.example.task.ActionType

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