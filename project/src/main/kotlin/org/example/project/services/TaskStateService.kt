package org.example.project.services

import org.example.project.BoardRepository
import org.example.project.BoardTaskStateNotFoundException
import org.example.project.BoardTaskStateRepository
import org.example.project.InvalidStatePositionException
import org.example.project.NotPermitedToTransferTaskException
import org.example.project.OrdersOfStatesIsIncorrectException
import org.example.project.OrganizationClient
import org.example.project.Permission
import org.example.project.ProjectRepository
import org.example.project.SecurityUtil
import org.example.project.StateNameExistsException
import org.example.project.TaskState
import org.example.project.TaskStateMapper
import org.example.project.TaskStateNotFoundException
import org.example.project.TaskStateRepository
import org.example.project.TaskStateTemplate
import org.example.project.TaskStateTemplateItem
import org.example.project.TaskStateTemplateItemRepository
import org.example.project.TaskStateTemplateRepository
import org.example.project.TemplateNameExistsException
import org.example.project.TemplateNotFoundException
import org.example.project.dtos.TaskStateCreateDto
import org.example.project.dtos.TaskStateFullResponseDto
import org.example.project.dtos.TaskStateShortResponseDto
import org.example.project.dtos.TaskStateTemplateCreateDto
import org.example.project.dtos.TaskStateTemplateItemResponseDto
import org.example.project.dtos.TaskStateTemplateResponseDto
import org.example.project.dtos.TaskStateUpdateDto
import org.example.project.dtos.TaskStateWithPositionDto
import org.example.project.dtos.TransferTaskCheckDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.abs

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

    fun createTemplate(dto: TaskStateTemplateCreateDto)
    fun getTemplates(): List<TaskStateTemplateResponseDto>
    fun createDefaultStates(organizationId: Long): List<TaskState>
}

@Service
class TaskStateServiceImpl(
    private val repository: TaskStateRepository,
    private val mapper: TaskStateMapper,
    private val organizationClient: OrganizationClient,
    private val securityUtil: SecurityUtil,
    private val boardTaskStateRepo: BoardTaskStateRepository,
    private val boardRepository: BoardRepository,
    private val projectRepository: ProjectRepository,
    private val templateRepository: TaskStateTemplateRepository,
    private val templateItemRepository: TaskStateTemplateItemRepository
) : TaskStateService {
    override fun create(dto: TaskStateCreateDto) {
        val organization = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId())
        val taskState = mapper.toEntity(dto, organization.organizationId)
        if(repository.existsByNameAndOrganizationIdAndDeletedFalse(taskState.name, organization.organizationId)) {
            throw StateNameExistsException()
        }
        repository.save(taskState)
    }

    override fun update(id: Long, dto: TaskStateUpdateDto) {
        val taskState = repository.findByIdAndDeletedFalse(id)
            ?: throw TaskStateNotFoundException()

        dto.name?.let {
            if(repository.existsByNameAndOrganizationIdAndDeletedFalse(dto.name, taskState.organizationId)) {
                throw StateNameExistsException()
            }
            taskState.name = it

        }
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
        val organizationId = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId())
        return repository.findAllByOrganizationIdAndDeletedFalse(organizationId.organizationId,pageable).map { mapper.toShortDto(it) }
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
        return repository.findAllByBoardIdAndDeletedFalse(boardId, pageable)
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
        if(abs(from.position - to.position) !=  1 ){
            throw OrdersOfStatesIsIncorrectException()
        }

        return true
    }

    @Transactional
    override fun createTemplate(dto: TaskStateTemplateCreateDto) {
        val organizationId = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId()).organizationId

        if (dto.states.isEmpty()) return

        val positions = dto.states.map { it.position }.sorted()
        if (positions.first() != 1) throw InvalidStatePositionException()
        for (i in 0 until positions.size - 1) {
            if (positions[i+1] != positions[i] + 1) {
                throw InvalidStatePositionException()
            }
        }

        if(templateRepository.existsByNameAndDeletedFalse(dto.name)) throw TemplateNameExistsException()

        val template = TaskStateTemplate(dto.name, organizationId)
        templateRepository.save(template)

        dto.states.forEach { itemDto ->
            val state = repository.findByIdAndDeletedFalse(itemDto.taskStateId)
                ?: throw TaskStateNotFoundException()
            val item = TaskStateTemplateItem(template, state, itemDto.position)
            templateItemRepository.save(item)
        }
    }

    override fun getTemplates(): List<TaskStateTemplateResponseDto> {
        val organizationId = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId()).organizationId
        return templateRepository.findAllByOrganizationIdAndDeletedFalse(organizationId).map { template ->
            val items = templateItemRepository.findAllByTemplateIdAndDeletedFalse(template.id!!)
            TaskStateTemplateResponseDto(
                id = template.id!!,
                name = template.name,
                states = items?.map { item ->
                    TaskStateTemplateItemResponseDto(
                        id = item.id!!,
                        taskState = mapper.toShortDto(item.taskState),
                        position = item.position
                    )
                } ?: throw TemplateNotFoundException()
            )
        }
    }

    @Transactional
    override fun createDefaultStates(organizationId: Long): List<TaskState> {
        val existing = repository.findByOrganizationIdAndDeletedFalse(organizationId, Pageable.unpaged())
        if (!existing.isEmpty) return existing.content

        val defaults = listOf(
            TaskState("To Do", "Default To Do State", Permission.ASSIGNED, organizationId),
            TaskState("In Progress", "Default In Progress State", Permission.ASSIGNED, organizationId),
            TaskState("Done", "Default Done State", Permission.ASSIGNED, organizationId)
        )
        return repository.saveAll(defaults)
    }

}
