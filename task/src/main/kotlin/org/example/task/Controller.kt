package org.example.task

import org.example.task.TaskCreateRequest
import org.example.task.TaskResponse
import org.example.task.TaskUpdateRequest
import org.example.task.services.TaskService
import org.springframework.data.domain.Page
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
@RequestMapping("/tasks")
class TaskController(
    private val service: TaskService
){

    @PreAuthorize("hasAnyRole('USER','DEVELOPER','ADMIN')")
    @PostMapping
    fun create(@RequestBody dto: TaskCreateRequest) = service.create(dto)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): TaskResponse = service.getOne(id)

    @GetMapping
    fun getAll(pageable: Pageable): Page<TaskResponse> = service.getAll(pageable)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: TaskUpdateRequest) = service.update(id, dto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PutMapping("/assign/{id}")
    fun assignEmployee(@PathVariable id: Long, @RequestBody employees: List<Long>) = service.assignEmployee(id, employees)

    @PutMapping("/unsign/{id}")
    fun unsignEmployee(@PathVariable id: Long, @RequestBody employees: List<Long>) = service.unsignEmployee(id, employees)

    @GetMapping("/get-task-actions/{id}")
    fun getTaskActions(@PathVariable id: Long, pageable: Pageable): Page<TaskHistoryResponse> = service.getTaskActions(id, pageable)
}

@RestController
@RequestMapping("internal/api/v1/tasks")
class TaskInternalController(
    private val service: TaskService
){

    @GetMapping("check-task/{taskId}")
    fun checkTask(@PathVariable taskId: Long) = service.checkTask(taskId)
}