package org.example.task.services

import jakarta.transaction.Transactional
import org.example.task.AttachClient
import org.example.task.FeignClientException
import org.example.task.GenerateHash
import org.example.task.ProjectClient
import org.example.task.SecurityUtil
import org.example.task.Task
import org.example.task.TaskAttachment
import org.example.task.TaskAttachmentRepository
import org.example.task.TaskNotFoundException
import org.example.task.TaskPriority
import org.example.task.TaskRepository
import org.example.task.dtos.InternalHashesCheckRequest
import org.example.task.dtos.RelationshipsCheckDto
import org.example.task.dtos.TaskCreateRequest
import org.example.task.dtos.TaskResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface TaskService {
    fun create(dto: TaskCreateRequest)
    fun getOne(id: Long): TaskResponse
    fun getAll(pageable: Pageable): Page<TaskResponse>
}

@Service
class TaskServiceImpl(
    private val repository: TaskRepository,
    private val taskAttachRepo: TaskAttachmentRepository,
    private val security: SecurityUtil,

    private val attachClient: AttachClient,
    private val projectClient: ProjectClient,
    private val hash: GenerateHash
) : TaskService {
    @Transactional
    override fun create(dto: TaskCreateRequest) {
        try {
            projectClient.checkTaskRelationships(RelationshipsCheckDto(
                dto.boardId, dto.stateId))

            val savedTask = repository.save(Task(
                boardId = dto.boardId,
                stateId = dto.stateId,
                title = dto.title,
                taskNumber = hash.generateHash(),
                description = dto.description,
                priority = TaskPriority.LOW,
                orderIndex = repository.getTaskLastOrderIndex() ?: 1,
                estimatedHours = dto.estimatedHours,
                deadline = dto.deadline,
                tags = dto.tags,
            ))

            val savingTaskAttach: MutableList<TaskAttachment> = mutableListOf()
            dto.attachHashes?.let { attachHashes ->

                if (dto.attachHashes!!.isNotEmpty()) {
                    val listExists = attachClient.listExists(
                        InternalHashesCheckRequest(
                            security.getCurrentUserId(),
                            dto.attachHashes!!
                        )
                    )
                }

                dto.attachHashes!!.forEach { attachHashFor ->
                    savingTaskAttach.add(TaskAttachment(
                        savedTask,
                        attachHashFor,
                    ))
                }
                taskAttachRepo.saveAll(savingTaskAttach)
            }

        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun getOne(id: Long): TaskResponse {
        repository.findByIdAndDeletedFalse(id)?.let { task ->
            return TaskResponse(
                id = task.id!!,
                boardId = task.boardId,
                stateId = task.stateId,
                title = task.title,
                description = task.description,
                priority = task.priority,
                estimatedHours = task.estimatedHours,
                deadline = task.deadline,
                tags = task.tags,
                attachHashes = taskAttachRepo.findTaskAttachmentByTaskId(task.id!!)
            )
        }
        throw TaskNotFoundException()
    }

    override fun getAll(pageable: Pageable): Page<TaskResponse> {
        val findAll = repository.findAll(pageable)

        return findAll.map { task ->
            TaskResponse(
                id = task.id!!,
                boardId = task.boardId,
                stateId = task.stateId,
                title = task.title,
                description = task.description,
                priority = task.priority,
                estimatedHours = task.estimatedHours,
                deadline = task.deadline,
                tags = task.tags,
                attachHashes = taskAttachRepo.findTaskAttachmentByTaskId(task.id!!)
            )
        }
    }
}