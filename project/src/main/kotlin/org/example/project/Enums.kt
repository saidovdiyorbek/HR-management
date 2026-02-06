package org.example.project

enum class Permission{
    OWNER, ASSIGNED
}


enum class ErrorCode(val code: Int) {
    PROJECT_NOT_FOUND(400),
    BOARD_NOT_FOUND(401),
    TASK_STATE_NOT_FOUND(402),
    BOARD_TASK_STATE_NOT_FOUND(403),
    PROJECT_IS_NOT_ACTIVE(404),
    PROJECT_ENDED(405),
    STATE_IS_NOT_FIRST(406),
    STATE_NOT_CONNNECTED_TO_BOARD(407),
}

enum class Role {
    USER,
    ADMIN,
    DEVELOPER,
}