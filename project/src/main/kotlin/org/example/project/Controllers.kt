package org.example.project

import org.example.project.dtos.AssignUsersToBoardDto
import org.example.project.dtos.BoardCreateDto
import org.example.project.dtos.BoardUpdateDto
import org.example.project.dtos.BoardTaskStateCreateDto
import org.example.project.dtos.BoardUserRequestDto
import org.example.project.dtos.ProjectCreateDto
import org.example.project.dtos.ProjectUpdateDto
import org.example.project.dtos.RelationshipsCheckDto
import org.example.project.dtos.TaskStateCreateDto
import org.example.project.dtos.TaskStateTemplateCreateDto
import org.example.project.dtos.TaskStateUpdateDto
import org.example.project.dtos.TransferTaskCheckDto
import org.example.project.services.BoardService
import org.example.project.services.ProjectService
import org.example.project.services.TaskStateService
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
class ProjectControllers(
    private val service: ProjectService
) {

    @PostMapping
    fun create(@RequestBody dto: ProjectCreateDto) = service.create(dto)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: ProjectUpdateDto) = service.update(id, dto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PutMapping("/{id}/close")
    fun close(@PathVariable id: Long) = service.close(id)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @PreAuthorize("hasAnyRole('DEVELOPER','ADMIN')")
    @GetMapping
    fun getAll(pageable: Pageable) = service.getAll(pageable)

    @GetMapping("/organization")
    fun getAllByOrganizationId(organizationId: Long?, pageable: Pageable) = service.getAllByOrganizationId(organizationId, pageable)
}


@RestController
@RequestMapping("/projects/boards")
class BoardController(
    private val service: BoardService
) {

    @PostMapping
    fun create(@RequestBody dto: BoardCreateDto) = service.create(dto)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: BoardUpdateDto) = service.update(id, dto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @PostMapping("/{id}/assign")
    fun assignUser(@PathVariable id: Long, @RequestBody dto: AssignUsersToBoardDto) = service.assignUsersToBoard(id, dto)

    @GetMapping
    fun getAll(pageable: Pageable) = service.getAll(pageable)
}

@RestController
@RequestMapping("/projects/task-states")
class TaskStateController(
    private val service: TaskStateService
) {

    @PostMapping
    fun create(@RequestBody dto: TaskStateCreateDto) = service.create(dto)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: TaskStateUpdateDto) = service.update(id, dto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @GetMapping
    fun getAll(pageable: Pageable) = service.getAll(pageable)

    @GetMapping("organization/{organizationId}")
    fun getAllByOrganizationId(@PathVariable organizationId: Long, pageable: Pageable) = service.getAllByOrganizationId(organizationId, pageable)

    @GetMapping("/board/{boardId}")
    fun getTaskStateWithPosition(@PathVariable boardId: Long, pageable: Pageable) = service.getAllByBoard( boardId, pageable)

    @GetMapping("/{stateId}/board/{boardId}")
    fun getTaskStateWithPosition(@PathVariable stateId: Long, @PathVariable boardId: Long) = service.getTaskStateWithPosition(stateId, boardId)
    
    // Templates
    @PostMapping("/templates")
    fun createTemplate(@RequestBody dto: TaskStateTemplateCreateDto) = service.createTemplate(dto)

    @GetMapping("/templates")
    fun getTemplates() = service.getTemplates()
}


@RestController
@RequestMapping("/internal/api/v1/projects")
class InternalController(
    private val boardService: BoardService,
    private val stateService: TaskStateService
) {

    @PostMapping("/check-relationships")
    fun checkRelationships(@RequestBody body: RelationshipsCheckDto) = boardService.checkRelationships(body)

    @PostMapping("/check-state-relationships")
    fun checkTransferStates(@RequestBody body: TransferTaskCheckDto): Boolean = stateService.transferTaskCheck(body)

    @PostMapping("/check-board-user-relationships")
    fun checkBoardUserRelationships(@RequestBody body: BoardUserRequestDto): Boolean = boardService.checkBoardUserRelationships(body)

    @GetMapping("/get-board-users/{boardId}")
    fun getBoardUsers(@PathVariable boardId: Long) = boardService.boardStateRelationshipsInfo(boardId)
}
