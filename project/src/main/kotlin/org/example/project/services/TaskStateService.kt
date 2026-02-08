package org.example.project.services

import org.example.project.BoardTaskStateNotFoundException
import org.example.project.NotPermitedToTransferTaskException
import org.example.project.OrdersOfStatesIsIncorrectException
import org.example.project.OrganizationClient
import org.example.project.TaskStateMapper
import org.example.project.TaskStateNotFoundException
import org.example.project.TaskStateRepository
import org.example.project.dtos.TaskStateCreateDto
import org.example.project.dtos.TaskStateFullResponseDto
import org.example.project.dtos.TaskStateShortResponseDto
import org.example.project.dtos.TaskStateUpdateDto
import org.example.project.dtos.TaskStateWithPositionDto
import org.example.project.dtos.TransferTaskCheckDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface TaskStateService {
    fun create(dto: TaskStateCreateDto)
    fun update(id: Long, dto: TaskStateUpdateDto)
    fun delete(id: Long)
    fun getById(id: Long): TaskStateFullResponseDto
    fun getAll(pageable: Pageable): Page<TaskStateShortResponseDto>
    fun getAllByOrganizationId(organizationId: Long, pageable: Pageable): Page<TaskStateShortResponseDto>
    fun getTaskStateWithPosition(stateId: Long, boardId: Long): TaskStateWithPositionDto
    fun getAllByBoard(boardId: Long, pageable: Pageable): Page<TaskStateShortResponseDto>
    fun transferTaskCheck(dto: TransferTaskCheckDto): Boolean
}

@Service
class TaskStateServiceImpl(
    private val repository: TaskStateRepository,
    private val mapper: TaskStateMapper,
    private val organizationClient: OrganizationClient
) : TaskStateService {
    override fun create(dto: TaskStateCreateDto) {
        val organization = organizationClient.getCurrentUserOrganization(getCurrentUserId())
        val taskState = mapper.toEntity(dto, organization.organizationId)
        repository.save(taskState)
    }

    override fun update(id: Long, dto: TaskStateUpdateDto) {
        val taskState = repository.findByIdAndDeletedFalse(id)
            ?: throw TaskStateNotFoundException()
        dto.name?.let { taskState.name = it }
        dto.description?.let { taskState.description = it }
        dto.permission?.let { taskState.permission = it }
        repository.save(taskState)
    }

    override fun delete(id: Long) {
        repository.trash(id)
    }

    override fun getById(id: Long): TaskStateFullResponseDto {
        val taskState = repository.findByIdAndDeletedFalse(id)
            ?: throw TaskStateNotFoundException()
        return mapper.toFullDto(taskState)
    }

    override fun getAll(pageable: Pageable): Page<TaskStateShortResponseDto> {
        return repository.findAllNotDeleted(pageable).map { mapper.toShortDto(it) }
    }

    override fun getAllByOrganizationId(
        organizationId: Long,
        pageable: Pageable
    ): Page<TaskStateShortResponseDto> {
        return repository.findByOrganizationIdAndDeletedFalse(organizationId, pageable)
            .map { mapper.toShortDto(it) }
    }

    override fun getTaskStateWithPosition(stateId: Long, boardId: Long): TaskStateWithPositionDto {
        val taskStateWithPositionDto= repository.findTaskStateWithPosition(stateId, boardId)
            ?: throw BoardTaskStateNotFoundException()

        return taskStateWithPositionDto

    }

    override fun getAllByBoard(
        boardId: Long,
        pageable: Pageable
    ): Page<TaskStateShortResponseDto> {
        return repository.findAllByBoardId(boardId, pageable)
            .map { mapper.toShortDto(it) }
    }

    override fun transferTaskCheck(dto: TransferTaskCheckDto): Boolean {
        val from = repository.findTaskStateWithPosition(dto.fromStateId, dto.boardId)
            ?: throw BoardTaskStateNotFoundException()
        val to = repository.findTaskStateWithPosition(dto.toStateId, dto.boardId)
            ?: throw BoardTaskStateNotFoundException()
        if(to.permission !=dto.permission) {
            throw NotPermitedToTransferTaskException()
        }
        if(from.position+1 != to.position){
            throw OrdersOfStatesIsIncorrectException()
        }

        return true
    }


    private fun getCurrentUserId(): Long {
        TODO("get user id from security context it implements later")
    }
}
