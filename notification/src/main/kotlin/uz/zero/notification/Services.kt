package uz.zero.notification

import org.springframework.stereotype.Service
import uz.zero.notification.dtos.TaskActionCreateDto
import uz.zero.notification.dtos.TaskActionEvent
import uz.zero.notification.listeners.TaskActionListener

interface TaskActionService {
    fun create(taskActionCreate: TaskActionCreateDto)
}

@Service
class TaskActionServiceImpl(
    private val taskListener: TaskActionListener,
) : TaskActionService {
    override fun create(taskActionCreate: TaskActionCreateDto) {
        taskActionCreate.run {
            taskListener.handleAction(
                TaskActionEvent(
                    this.taskId,
                    this.userId,
                    this.type,
                    this.details,
                )
            )
        }
    }
}