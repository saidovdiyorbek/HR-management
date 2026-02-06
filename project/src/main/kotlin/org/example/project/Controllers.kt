package org.example.project

import org.example.project.dtos.BoardCreateDto
import org.example.project.dtos.BoardUpdateDto
import org.example.project.dtos.BoardTaskStateCreateDto
import org.example.project.dtos.ProjectCreateDto
import org.example.project.dtos.ProjectUpdateDto
import org.example.project.dtos.RelationshipsCheckDto
import org.example.project.dtos.TaskStateCreateDto
import org.example.project.dtos.TaskStateShortResponseDto
import org.example.project.dtos.TaskStateUpdateDto
import org.example.project.services.BoardService
import org.example.project.services.BoardTaskStateService
import org.example.project.services.ProjectService
import org.example.project.services.TaskStateService
import org.springframework.data.domain.Pageable
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

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @GetMapping
    fun getAll(pageable: Pageable) = service.getAll(pageable)
}

@RestController
@RequestMapping("/boards")
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

    @GetMapping
    fun getAll(pageable: Pageable) = service.getAll(pageable)
}

@RestController
@RequestMapping("/task-states")
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
}

@RestController
@RequestMapping("/board-task-states")
class BoardTaskStateController(
    private val service: BoardTaskStateService
) {

    @PostMapping
    fun create(@RequestBody dto: BoardTaskStateCreateDto) = service.create(dto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}

//Internal
@RestController
@RequestMapping("/internal/api/v1/posts")
class BoardInternalController(
    private val service: BoardService
) {

    @PostMapping("/check-relationships")
    fun checkRelationships(@RequestBody body: RelationshipsCheckDto): Boolean = service.checkRelationships(body)


}
