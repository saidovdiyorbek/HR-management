package uz.zero.notification

enum class TaskPriority{
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
    CRITICAL
}

enum class ErrorCode(val code: Int, val message: String) {
    TASK_NOT_FOUND(100, "TASK_NOT_FOUND"),
    THIS_TASK_NOT_YOURS(102, "THIS_TASK_NOT_YOURS"),
    SOMETHING_WENT_WRONG(103, "SOMETHING_WENT_WRONG"),
    NOT_CONNECTED_TELEGRAM_BOT(100, "NOT_CONNECTED_TELEGRAM_BOT"),
}

enum class Role {
    USER,
    ADMIN,
    DEVELOPER,
}

enum class Permission{
    OWNER, ASSIGNED
}

enum class EmployeeRole {
    CEO,
    EMPLOYEE
}

enum class ActionType{
    CREATED,
    MOVED,
    STATUS_CHANGED,
    COMMENT_ADDED,
    DELETED
}