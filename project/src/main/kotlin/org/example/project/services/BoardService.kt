package org.example.project.services

import org.example.project.BoardMapper
import org.example.project.BoardNotFoundException
import org.example.project.BoardRepository
import org.example.project.BoardTaskState
import org.example.project.BoardTaskStateRepository
import org.example.project.InvalidStatePositionException
import org.example.project.ProjectEndException
import org.example.project.ProjectIsNotActiveException
import org.example.project.ProjectNotFoundException
import org.example.project.ProjectRepository
import org.example.project.SecurityUtil
import org.example.project.StateIsNotFirstException
import org.example.project.StateNotConnnectedToBoardException
import org.example.project.TaskStateRepository
import org.example.project.TaskStateNotFoundException
import org.example.project.TaskStateTemplateItemRepository
import org.example.project.TemplateNotFoundException
import org.example.project.dtos.BoardCreateDto
import org.example.project.dtos.BoardFullResponseDto
import org.example.project.dtos.BoardShortResponseDto
import org.example.project.dtos.BoardTaskStateDefinitionDto
import org.example.project.dtos.BoardUpdateDto
import org.example.project.dtos.RelationshipsCheckDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface BoardService {
    fun create(dto: BoardCreateDto)
    fun update(id: Long, dto: BoardUpdateDto)
    fun delete(id: Long)
    fun getById(id: Long): BoardFullResponseDto
    fun getAll(pageable: Pageable): Page<BoardShortResponseDto>
    fun checkRelationships(body: RelationshipsCheckDto): Boolean
}

@Service
class BoardServiceImpl(
    private val repository: BoardRepository,
    private val mapper: BoardMapper,
    private val taskStateRepository: TaskStateRepository,
    private val projectRepository: ProjectRepository,
    private val securityUtil: SecurityUtil,
    private val boardTaskStateRepository: BoardTaskStateRepository,
    private val templateItemRepository: TaskStateTemplateItemRepository
) : BoardService {
    @Transactional
    override fun create(dto: BoardCreateDto) {
        val project = projectRepository.findByIdAndDeletedFalse(dto.projectId)
            ?: throw ProjectNotFoundException()
        if(project.endDate != null) {
            throw ProjectEndException()
        }
        val board = mapper.toEntity(dto, project)
        repository.save(board)
        
        val statesToLink = mutableListOf<BoardTaskStateDefinitionDto>()
        dto.states?.let { statesToLink.addAll(it) }

        dto.templateId?.let { tid ->
            val items = templateItemRepository.findAllByTemplateIdAndDeletedFalse(tid)

            statesToLink.addAll(items.map {
                BoardTaskStateDefinitionDto(it.taskState.id!!, it.position)
            })
        }?: throw TemplateNotFoundException()

        if (statesToLink.isNotEmpty()) {
            val positions = statesToLink.map { it.position }.sorted()
            if (positions.first() != 1) throw InvalidStatePositionException()
            for (i in 0 until positions.size - 1) {
                if (positions[i+1] != positions[i] + 1) {
                    throw InvalidStatePositionException()
                }
            }
        }

        statesToLink.forEach { stateDef ->
            val taskState = taskStateRepository.findByIdAndDeletedFalse(stateDef.stateId)
                ?: throw TaskStateNotFoundException()
             
            val boardTaskState = BoardTaskState(board, taskState, stateDef.position)
            boardTaskStateRepository.save(boardTaskState)
        }
    }

    override fun update(id: Long, dto: BoardUpdateDto) {
        val board = repository.findByIdAndDeletedFalse(id)
            ?: throw BoardNotFoundException()
        dto.name?.let { board.name = it }
        dto.description?.let { board.description = it }
        repository.save(board)
    }

    override fun delete(id: Long) {
        repository.trash(id)
    }

    override fun getById(id: Long): BoardFullResponseDto {
        val board = repository.findByIdAndDeletedFalse(id)
            ?: throw BoardNotFoundException()
        return mapper.toFullDto(board, emptyList())
    }

    override fun getAll(pageable: Pageable): Page<BoardShortResponseDto> {
        return repository.findAllNotDeleted(pageable).map { mapper.toShortDto(it) }
    }

    override fun checkRelationships(body: RelationshipsCheckDto): Boolean {
        repository.findByIdAndDeletedFalse(body.boardId)?.let{ board ->
            projectRepository.findByIdAndDeletedFalse(board.project.id!!)?.let { project ->
                if (project.endDate != null) {
                    throw ProjectEndException()
                }
                if(!project.isActive){
                    throw ProjectIsNotActiveException()
                }
                val stateOrder =taskStateRepository.findTaskStateWithPosition(body.stateId, body.boardId)
                    ?:throw StateNotConnnectedToBoardException()

                if(stateOrder.position !=1){
                    throw StateIsNotFirstException()
                }
                return true
            }
            throw BoardNotFoundException()
        }
        throw ProjectNotFoundException()
    }
}
