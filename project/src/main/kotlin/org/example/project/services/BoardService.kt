package org.example.project.services

import org.example.project.Board
import org.example.project.BoardMapper
import org.example.project.BoardNameAlreadyExistsInProjectException
import org.example.project.BoardNotFoundException
import org.example.project.BoardRepository
import org.example.project.BoardTaskState
import org.example.project.BoardTaskStateRepository
import org.example.project.BoardUser
import org.example.project.BoardUserRepository
import org.example.project.EmployeeCannotCreateBoardException
import org.example.project.EmployeeClient
import org.example.project.EmployeeRole
import org.example.project.FeignClientException
import org.example.project.InvalidStatePositionException
import org.example.project.OrganizationClient
import org.example.project.Project
import org.example.project.ProjectEndException
import org.example.project.ProjectIsNotActiveException
import org.example.project.ProjectNotFoundException
import org.example.project.ProjectRepository
import org.example.project.SecurityUtil
import org.example.project.StateAlreadyConnectedBoardException
import org.example.project.StateIsNotFirstException
import org.example.project.StateNotConnnectedToBoardException
import org.example.project.TaskStateRepository
import org.example.project.TaskStateNotFoundException
import org.example.project.TaskStateTemplateItemRepository
import org.example.project.TaskStateTemplateRepository
import org.example.project.TemplateNotFoundException
import org.example.project.UserAlreadyAssignedToBoardException
import org.example.project.UserNotAssignedToBoardException
import org.example.project.UserNotCEOException
import org.example.project.dtos.AssignUsersToBoardDto
import org.example.project.dtos.BoardCreateDto
import org.example.project.dtos.BoardFullResponseDto
import org.example.project.dtos.BoardInfoDto
import org.example.project.dtos.BoardShortResponseDto
import org.example.project.dtos.BoardTaskStateDefinitionDto
import org.example.project.dtos.BoardUpdateDto
import org.example.project.dtos.BoardUserRequestDto
import org.example.project.dtos.CheckResponse
import org.example.project.dtos.CheckUsersInOrganizationRequest
import org.example.project.dtos.ProjectShortInfo
import org.example.project.dtos.RelationshipsCheckDto
import org.example.project.dtos.RequestEmployeeRole
import org.example.project.dtos.StatePositionUpdateDto
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
    fun checkRelationships(body: RelationshipsCheckDto): CheckResponse
    fun assignUsersToBoard(boardId: Long, dto: AssignUsersToBoardDto)
    fun checkBoardUserRelationships(body: BoardUserRequestDto): Boolean
    fun removeStatesFromBoard(boardId: Long, stateId: Long)
    fun addStatesToBoard(boardId: Long, states: List<Long>)
    fun boardStateRelationshipsInfo(boardId: Long): BoardInfoDto
    fun updateStatePositions(boardId: Long, stateUpdate: StatePositionUpdateDto)
    fun getProjectByBoardId(boardId: Long): ProjectShortInfo
}

@Service
class BoardServiceImpl(
    private val repository: BoardRepository,
    private val mapper: BoardMapper,
    private val taskStateRepository: TaskStateRepository,
    private val projectRepository: ProjectRepository,
    private val securityUtil: SecurityUtil,
    private val boardTaskStateRepository: BoardTaskStateRepository,
    private val templateItemRepository: TaskStateTemplateItemRepository,
    private val employeeClient: EmployeeClient,
    private val boardUserRepository: BoardUserRepository,
    private val templateRepository: TaskStateTemplateRepository,
    private val organizationClient: OrganizationClient
) : BoardService {

    @Transactional
    override fun create(dto: BoardCreateDto) {
        val project = projectRepository.findByIdAndDeletedFalse(dto.projectId)
            ?: throw ProjectNotFoundException()

        project.endDate?.let { throw ProjectEndException() }

        testNameIsUniqueInProject(dto.name, project)

        testIsUserCeoInThisCompany(project)

        val board = mapper.toEntity(dto, project)
        repository.save(board)

        saveAllToBoardTaskState(dto, board)

    }


    @Transactional
    override fun update(id: Long, dto: BoardUpdateDto) {
        val board = repository.findByIdAndDeletedFalse(id)
            ?: throw BoardNotFoundException()

        testIsUserCeoInThisCompany(board.project)

        dto.name?.let {
            testNameIsUniqueInProject(it, board.project)
            board.name = it
        }
        dto.description?.let { board.description = it }
        repository.save(board)
    }

    override fun delete(id: Long) {
        repository.trash(id)
    }

    override fun getById(id: Long): BoardFullResponseDto {
        val board = repository.findByIdAndDeletedFalse(id)
            ?: throw BoardNotFoundException()
        val assignedUsers = boardUserRepository.findByBoardIdAndDeletedFalse(id).map { it.userId }
        return mapper.toFullDto(board, emptyList(), assignedUsers)
    }


    override fun getAll(pageable: Pageable): Page<BoardShortResponseDto> {
        try {
            val org = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId())
            val role = employeeClient.getEmployeeRoleByUserId(
                securityUtil.getCurrentUserId(),
                RequestEmployeeRole(securityUtil.getCurrentUserId(), org.organizationId)
            )
            if (role.employeeRole != EmployeeRole.CEO) {
                throw UserNotCEOException()
            }
        } catch (e: FeignClientException) {
            throw e
        }

        return repository.findAllNotDeleted(pageable).map { mapper.toShortDto(it) }
    }


    override fun checkRelationships(body: RelationshipsCheckDto): CheckResponse {
        repository.findByIdAndDeletedFalse(body.boardId)?.let { board ->
            projectRepository.findByIdAndDeletedFalse(board.project.id!!)?.let { project ->
                if (project.endDate != null) {
                    throw ProjectEndException()
                }
                if (!project.isActive) {
                    throw ProjectIsNotActiveException()
                }
                val stateOrder = taskStateRepository.findTaskStateWithPosition(body.stateId, body.boardId)
                    ?: throw StateNotConnnectedToBoardException()
                if(!body.isUpdate){
                    if (stateOrder.position != 1) {
                        throw StateIsNotFirstException()
                    }
                }

                return CheckResponse(project.organizationId)
            }
            throw BoardNotFoundException()
        }
        throw ProjectNotFoundException()
    }

    override fun assignUsersToBoard(boardId: Long, dto: AssignUsersToBoardDto) {
        val board = repository.findByIdAndDeletedFalse(boardId)
            ?: throw BoardNotFoundException()

        employeeClient.checkUsersInOrganization(
            CheckUsersInOrganizationRequest(
                board.project.organizationId,
                dto.userIds
            )
        )

        testIsUserCeoInThisCompany(board.project)

        dto.userIds.forEach { userId ->
            if (!boardUserRepository.existsByBoardIdAndUserIdAndDeletedFalse(boardId, userId)) {
                boardUserRepository.save(BoardUser(board, userId))
            } else {
                throw UserAlreadyAssignedToBoardException()
            }
        }

    }

    override fun checkBoardUserRelationships(body: BoardUserRequestDto): Boolean {
        repository.findByIdAndDeletedFalse(body.boardId)?.let { board ->
            projectRepository.findByIdAndDeletedFalse(board.project.id!!)?.let { project ->
                if (project.endDate != null) {
                    throw ProjectEndException()
                }
                if (!project.isActive) {
                    throw ProjectIsNotActiveException()
                }
                val assignedUsers = boardUserRepository.findByBoardIdAndDeletedFalse(body.boardId).map { it.userId }
                if (!assignedUsers.containsAll(body.userIds)) {
                    throw UserNotAssignedToBoardException()
                }
                return true
            }
            throw BoardNotFoundException()
        }
        throw ProjectNotFoundException()
    }


    override fun removeStatesFromBoard(boardId: Long, stateId: Long) {
        val board = repository.findByIdAndDeletedFalse(boardId)
            ?: throw BoardNotFoundException()

        testIsUserCeoInThisCompany(board.project)

        val stateToRemove = boardTaskStateRepository.findByBoardIdAndDeletedFalse(boardId)
            .find { it.taskState.id == stateId }
            ?: throw StateNotConnnectedToBoardException()

        val removedPosition = stateToRemove.position

        boardTaskStateRepository.trash(stateToRemove.id!!)

        val statesToUpdate = boardTaskStateRepository.findByBoardIdAndDeletedFalse(boardId)
            .filter { it.position > removedPosition }

        statesToUpdate.forEach { state ->
            state.position -= 1
            boardTaskStateRepository.save(state)
        }
    }

    override fun addStatesToBoard(
        boardId: Long,
        states: List<Long>
    ) {
        val board = repository.findByIdAndDeletedFalse(boardId)
            ?: throw BoardNotFoundException()

        testIsUserCeoInThisCompany(board.project)

        var currentMaxPosition = boardTaskStateRepository.findMaxPosition(boardId) ?: 0

        states.forEach { stateId ->
            val taskState = taskStateRepository.findByIdAndDeletedFalse(stateId)
                ?: throw TaskStateNotFoundException()

            if (boardTaskStateRepository.existsByBoardAndTaskStateAndDeletedFalse(board, taskState)) {
                throw StateAlreadyConnectedBoardException()
            }

            currentMaxPosition += 1

            val boardTaskState = BoardTaskState(board, taskState, currentMaxPosition)
            boardTaskStateRepository.save(boardTaskState)
        }

    }

    override fun boardStateRelationshipsInfo(boardId: Long): BoardInfoDto {
        val board = repository.findByIdAndDeletedFalse(boardId)
            ?: throw BoardNotFoundException()

        val states = boardTaskStateRepository.findByBoardIdWithStateAndDeletedFalse(boardId)

        return BoardInfoDto(board.id!!, board.name, states)
    }

    @Transactional
    override fun updateStatePositions(
        boardId: Long,
        stateUpdate: StatePositionUpdateDto
    ) {
        val board = repository.findByIdAndDeletedFalse(boardId)
            ?: throw BoardNotFoundException()

        testIsUserCeoInThisCompany(board.project)

        val maxPosition = boardTaskStateRepository.findMaxPosition(boardId) ?: 0

        if (stateUpdate.newPosition !in 1..maxPosition) {
            throw InvalidStatePositionException()
        }

        val allBoardTaskStates = boardTaskStateRepository.findByBoardIdAndDeletedFalse(boardId)

        val stateToUpdate = allBoardTaskStates.find { it.taskState.id == stateUpdate.stateId }
            ?: throw StateNotConnnectedToBoardException()

        val oldPosition = stateToUpdate.position
        val newPosition = stateUpdate.newPosition

        if (oldPosition != newPosition) {
            if (oldPosition < newPosition) {
                val statesToShift = allBoardTaskStates.filter {
                    it.position in (oldPosition + 1)..newPosition
                }
                statesToShift.forEach { state ->
                    state.position -= 1
                    boardTaskStateRepository.save(state)
                }
            } else {
                val statesToShift = allBoardTaskStates.filter {
                    it.position in newPosition..<oldPosition
                }
                statesToShift.forEach { state ->
                    state.position += 1
                    boardTaskStateRepository.save(state)
                }
            }

            // Update the target state to new position
            stateToUpdate.position = newPosition
            boardTaskStateRepository.save(stateToUpdate)
        }
    }

    override fun getProjectByBoardId(boardId: Long): ProjectShortInfo {
        val board = repository.findByIdAndDeletedFalse(boardId)

        return ProjectShortInfo(
            board?.project?.id,
            board?.project?.name ?: throw ProjectNotFoundException(),
            boardStateRelationshipsInfo(boardId)
        )
    }


    private fun saveAllToBoardTaskState(dto: BoardCreateDto, board: Board) {
        val statesToLink = mutableListOf<BoardTaskStateDefinitionDto>()

        saveAllStatesToList(dto, statesToLink)

        testPositions(statesToLink, board)

        saveAllToBoardTaskState(board, statesToLink)
    }

    private fun saveAllStatesToList(
        dto: BoardCreateDto,
        statesToLink: MutableList<BoardTaskStateDefinitionDto>
    ) {
        dto.states?.let {
            dto.states.let { statesToLink.addAll(it) }
        } ?: run {
            dto.templateId?.let { tid ->
                templateRepository.findByIdAndDeletedFalse(tid) ?: throw TemplateNotFoundException()
                val items = templateItemRepository.findAllByTemplateIdAndDeletedFalse(tid)
                    ?: throw TemplateNotFoundException()

                statesToLink.addAll(items.map {
                    BoardTaskStateDefinitionDto(it.taskState.id!!, it.position)
                })
            }
        }
    }

    private fun testPositions(statesToLink: MutableList<BoardTaskStateDefinitionDto>, board: Board) {
        if (statesToLink.isEmpty()) return

        val sortedStates = statesToLink.sortedBy { it.position }

        if (sortedStates.first().position != 1) {
            throw InvalidStatePositionException()
        }

        for (i in 0 until sortedStates.size - 1) {
            if (sortedStates[i + 1].position != sortedStates[i].position + 1) {
                throw InvalidStatePositionException()
            }
        }

        statesToLink.forEach { stateDef ->
            taskStateRepository.findByIdAndDeletedFalse(stateDef.stateId)
                ?: throw TaskStateNotFoundException()
        }
    }

    private fun saveAllToBoardTaskState(
        board: Board,
        statesToLink: MutableList<BoardTaskStateDefinitionDto>
    ) {
        statesToLink.forEach { stateDef ->
            val taskState = taskStateRepository.findByIdAndDeletedFalse(stateDef.stateId)
                ?: throw TaskStateNotFoundException()

            if (boardTaskStateRepository.existsByBoardAndTaskStateAndDeletedFalse(board, taskState)) {
                throw StateAlreadyConnectedBoardException()
            }

            val boardTaskState = BoardTaskState(board, taskState, stateDef.position)
            boardTaskStateRepository.save(boardTaskState)
        }
    }

    private fun testIsUserCeoInThisCompany(project: Project) {
        val role = employeeClient.getEmployeeRoleByUserId(
            securityUtil.getCurrentUserId(),
            RequestEmployeeRole(securityUtil.getCurrentUserId(), project.organizationId)
        )

        if (role.employeeRole != EmployeeRole.CEO) {
            throw EmployeeCannotCreateBoardException()
        }
    }

    private fun testNameIsUniqueInProject(name: String, project: Project) {
        if (repository.existsByNameAndProjectAndDeletedIsFalse(name, project)) {
            throw BoardNameAlreadyExistsInProjectException()
        }
    }
}
