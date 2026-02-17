package uz.zero.notification.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.notification.TaskActionService
import uz.zero.notification.dtos.TaskActionCreateDto

@RestController
@RequestMapping("/internal/api/v1/notifications")
class InternalNotificationController(
    private val service: TaskActionService
) {
    @PostMapping("/create-action")
    fun createAction(@RequestBody taskActionCreate: TaskActionCreateDto) = service.create(taskActionCreate)
}