package org.example.task

enum class TaskPriority{
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
    CRITICAL
}

enum class ErrorCode(val code: Int, val message: String) {
    TASK_NOT_FOUND(100, "TASK_NOT_FOUND"),
}

enum class Role {
    USER,
    ADMIN,
    DEVELOPER,
}

enum class Permission{
    OWNER, ASSIGNED
}