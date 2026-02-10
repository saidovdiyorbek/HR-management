package org.example.organization

enum class EmployeeRole {
    CEO,
    EMPLOYEE
}
enum class ErrorCode(val code: Int) {
    ORGANIZATION_ALREADY_EXISTS(100),
    ORGANIZATION_NOT_FOUND(101),
    ORGANIZATION_NOT_ACTIVE(103),

    ////////

    EMPLOYEE_ALREADY_EXISTS(200),
    EMPLOYEE_NOT_FOUND(201),
    EMPLOYEE_CONTEX_NOT_FOUND(202),
    EMPLOYEE_NOT_IN_ORGANIZATION(203),
    USER_NOT_FOUND(204)

}

enum class Role {
    USER,
    ADMIN,
    DEVELOPER,
}