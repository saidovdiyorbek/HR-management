package org.example.task

import org.example.task.dtos.TaskCreateRequest
import org.example.task.services.TaskService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val service: TaskService
){

    @PostMapping
    fun create(@RequestBody dto: TaskCreateRequest) = service.create(dto)

}