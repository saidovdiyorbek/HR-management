package org.example.project

enum class Permission{
    OWNER, ASSIGNED
}


enum class ErrorCode(val code: Int) {
    PROJECT_NOT_FOUND(400),
}