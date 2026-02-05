package org.example.organization

enum class EmployeeRole {
    CEO,
    EMPLOYEE
}
enum class ErrorCode(val code: Long) {
    ORGANIZATION_ALREADY_EXISTS(100),
    ORGANIZATION_NOT_FOUND(101),

}