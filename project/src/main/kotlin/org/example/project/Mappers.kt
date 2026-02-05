package org.example.project

import org.example.project.dtos.BoardCreateDto
import org.example.project.dtos.BoardFullResponseDto
import org.example.project.dtos.BoardShortResponseDto
import org.example.project.dtos.BoardTaskStateCreateDto
import org.example.project.dtos.ProjectCreateDto
import org.example.project.dtos.ProjectFullResponseDto
import org.example.project.dtos.ProjectShortResponseDto
import org.example.project.dtos.TaskStateCreateDto
import org.example.project.dtos.TaskStateFullResponseDto
import org.example.project.dtos.TaskStateShortResponseDto
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProjectMapper(
    private val boardMapper: BoardMapper
) {

    fun toEntity(dto: ProjectCreateDto, createDate: LocalDate, organizationId: Long): Project {
        return Project(
            name = dto.name,
            description = dto.description,
            startDate = createDate,
            endDate = null,
            organizationId = organizationId
        )
    }

    fun toShortDto(project: Project): ProjectShortResponseDto {
        return ProjectShortResponseDto(
            id = project.id!!,
            name = project.name
        )
    }

    fun toFullDto(project: Project, boards: List<Board>): ProjectFullResponseDto {
        return ProjectFullResponseDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            startDate = project.startDate,
            endDate = project.endDate,
            organizationId = project.organizationId,
            boards = boards.map { boardMapper.toShortDto(it) }.toList(),
            isActive = project.isActive,
            createdAt = project.createdDate,
            updatedAt = project.modifiedDate
        )
    }
}

@Component
class BoardMapper(
    private val taskStateMapper: TaskStateMapper
) {

    fun toEntity(dto: BoardCreateDto, project: Project): Board {
        return Board(
            name = dto.name,
            description = dto.description,
            project = project
        )
    }

    fun toShortDto(board: Board): BoardShortResponseDto {
        return BoardShortResponseDto(
            id = board.id!!,
            name = board.name
        )
    }

    fun toFullDto(board: Board, taskStates: List<TaskState>): BoardFullResponseDto {
        return BoardFullResponseDto(
            id = board.id!!,
            name = board.name,
            description = board.description ?: "",
            taskStates = taskStates.map { taskStateMapper.toShortDto(it) }.toList(),
            createdAt = board.createdDate!!,
            updatedAt = board.modifiedDate!!
        )
    }
}

@Component
class TaskStateMapper {

    fun toEntity(dto: TaskStateCreateDto, companyId: Long): TaskState {
        return TaskState(
            name = dto.name,
            description = dto.description,
            permission = dto.permission,
            companyId = companyId
        )
    }

    fun toShortDto(taskState: TaskState): TaskStateShortResponseDto {
        return TaskStateShortResponseDto(
            id = taskState.id!!,
            name = taskState.name
        )
    }

    fun toFullDto(taskState: TaskState): TaskStateFullResponseDto {
        return TaskStateFullResponseDto(
            id = taskState.id!!,
            name = taskState.name,
            description = taskState.description ?: "",
            permission = taskState.permission,
            companyId = taskState.companyId
        )
    }
}

@Component
class BoardTaskStateMapper {

    fun toEntity(dto: BoardTaskStateCreateDto, board: Board, taskState: TaskState): BoardTaskState {
        return BoardTaskState(
            board = board,
            taskState = taskState,
            position = dto.position
        )
    }
}
