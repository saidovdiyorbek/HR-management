package org.example.project

enum class Permission{
    OWNER, ASSIGNED
}


enum class ErrorCode(val code: Int) {
    PROJECT_NOT_FOUND(400),
    BOARD_NOT_FOUND(401),
    TASK_STATE_NOT_FOUND(402),
    BOARD_TASK_STATE_NOT_FOUND(403),
}