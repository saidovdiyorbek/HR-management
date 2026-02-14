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
    SOMETHING_WENT_WRONG(103, "SOMETHING_WENT_WRONG")
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