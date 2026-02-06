package org.example.task.services

import jakarta.transaction.Transactional
import org.example.task.FeignClientException
import org.example.task.ProjectClient
import org.example.task.TaskRepository
import org.example.task.dtos.RelationshipsCheckDto
import org.example.task.dtos.TaskCreateRequest
import org.springframework.stereotype.Service

interface TaskService {
    fun create(dto: TaskCreateRequest)
}

@Service
class TaskServiceImpl(
    private val repository: TaskRepository,

    private val projectClient: ProjectClient
) : TaskService {
    @Transactional
    override fun create(dto: TaskCreateRequest) {
        try {
            projectClient.checkTaskRelationships(RelationshipsCheckDto(
                dto.boardId, dto.stateId))

        }catch (e: FeignClientException){
            throw e
        }
    }

}