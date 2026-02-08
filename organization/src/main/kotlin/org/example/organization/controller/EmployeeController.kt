package org.example.organization.controller

import org.example.organization.dto.EmployeeCreateRequest
import org.example.organization.dto.EmployeeResponse
import org.example.organization.dto.EmployeeRoleResponse
import org.example.organization.dto.EmployeeRoleUpdateRequest
import org.example.organization.dto.EmployeeUpdateRequest
import org.example.organization.service.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/organizations/employees/{organizationId}")
class EmployeeController(
    private val service: EmployeeService
) {

    @PostMapping
    fun addEmployee(
        @PathVariable organizationId: Long,
        @RequestBody body: EmployeeCreateRequest,
        @RequestParam(required = false) createdByUserId: Long?
    ) = service.addEmployee(organizationId, body, createdByUserId)

    @GetMapping
    fun getEmployeesByOrganization(
        @PathVariable organizationId: Long
    ): List<EmployeeResponse> =
        service.getEmployeesByOrganization(organizationId)

    @PutMapping("/{userId}")
    fun updateEmployee(
        @PathVariable organizationId: Long,
        @PathVariable userId: Long,
        @RequestBody body: EmployeeUpdateRequest
    ): ResponseEntity<Unit> {
        service.updateEmployee(organizationId, userId, body)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{userId}")
    fun removeEmployee(
        @PathVariable organizationId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Unit> {
        service.removeEmployee(organizationId, userId)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{userId}/role")
    fun updateEmployeeRole(
        @PathVariable organizationId: Long,
        @PathVariable userId: Long,
        @RequestBody body: EmployeeRoleUpdateRequest
    ) = service.updateEmployeeRole(organizationId, userId, body)
}

@RestController
@RequestMapping("/internal/api/v1/employees")
class EmployeeInternalController(
    private val service: EmployeeService
) {
    @GetMapping("/get-employee-role/{userId}")
    fun getEmployeeRoleByUserId(@PathVariable userId: Long): EmployeeRoleResponse = service.getEmployeeRoleByUserId(userId)

}
