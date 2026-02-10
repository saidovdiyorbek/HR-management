package org.example.organization.controller

import org.example.organization.dto.AllEmployeesResponse
import org.example.organization.dto.CheckUsersInOrganizationRequest
import org.example.organization.dto.CheckUsersInOrganizationResponse
import org.example.organization.dto.EmployeeCreateRequest
import org.example.organization.dto.EmployeeResponse
import org.example.organization.dto.EmployeeRoleResponse
import org.example.organization.dto.EmployeeRoleUpdateRequest
import org.example.organization.dto.EmployeeUpdateRequest
import org.example.organization.dto.RequestEmployeeRole
import org.example.organization.service.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeController(
    private val service: EmployeeService
) {

    @PostMapping("/organizations/{organizationId}")
    fun addEmployee(
        @PathVariable organizationId: Long,
        @RequestBody body: EmployeeCreateRequest,
        @RequestParam(required = false) createdByUserId: Long?
    ) = service.addEmployee(organizationId, body, createdByUserId)

    @GetMapping("/organizations/{organizationId}")
    fun getEmployeesByOrganization(
        @PathVariable organizationId: Long
    ): List<EmployeeResponse> =
        service.getEmployeesByOrganization(organizationId)

    @PutMapping("/organizations/{organizationId}/{userId}")
    fun updateEmployee(
        @PathVariable organizationId: Long,
        @PathVariable userId: Long,
        @RequestBody body: EmployeeUpdateRequest
    ): ResponseEntity<Unit> {
        service.updateEmployee(organizationId, userId, body)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/organizations/{organizationId}/{userId}")
    fun removeEmployee(
        @PathVariable organizationId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Unit> {
        service.removeEmployee(organizationId, userId)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/organizations/{organizationId}/{userId}/role")
    fun updateEmployeeRole(
        @PathVariable organizationId: Long,
        @PathVariable userId: Long,
        @RequestBody body: EmployeeRoleUpdateRequest
    ) = service.updateEmployeeRole(organizationId, userId, body)

    // dbdagi barcha employeelarni olish
    @GetMapping("/all")
    fun getAllEmployees(): List<AllEmployeesResponse> = service.getAllEmployees()
}

@RestController
@RequestMapping("/internal/api/v1/employees")
class EmployeeInternalController(
    private val service: EmployeeService
) {
    @GetMapping("/get-employee-role/{userId}")
    fun getEmployeeRole(@PathVariable userId: Long, @RequestBody dto: RequestEmployeeRole): EmployeeRoleResponse = service.getEmployeeRole(dto)

    @PostMapping("/check-users-in-organization")
    fun checkUsersInOrganization(
        @RequestBody dto: CheckUsersInOrganizationRequest
    ): Boolean = service.areAllUsersInOrganization(dto.organizationId, dto.userIds)
}
