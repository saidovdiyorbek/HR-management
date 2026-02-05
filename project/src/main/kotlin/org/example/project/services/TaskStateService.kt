package org.example.project.services

import org.example.project.OrganizationClient
import org.example.project.TaskStateMapper
import org.example.project.TaskStateNotFoundException
import org.example.project.TaskStateRepository
import org.example.project.dtos.TaskStateCreateDto
import org.example.project.dtos.TaskStateFullResponseDto
import org.example.project.dtos.TaskStateShortResponseDto
import org.example.project.dtos.TaskStateUpdateDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface TaskStateService {
    fun create(dto: TaskStateCreateDto)
    fun update(id: Long, dto: TaskStateUpdateDto)
    fun delete(id: Long)
    fun getById(id: Long): TaskStateFullResponseDto
    fun getAll(pageable: Pageable): Page<TaskStateShortResponseDto>
}

@Service
class TaskStateServiceImpl(
    private val repository: TaskStateRepository,
    private val mapper: TaskStateMapper,
    private val organizationClient: OrganizationClient
) : TaskStateService {
    override fun create(dto: TaskStateCreateDto) {
        val organization = organizationClient.getCurrentUserOrganization(getCurrentUserId())
        val taskState = mapper.toEntity(dto, organization.id)
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

    private fun getCurrentUserId(): Long {
        TODO("get user id from security context it implements later")
    }
}
