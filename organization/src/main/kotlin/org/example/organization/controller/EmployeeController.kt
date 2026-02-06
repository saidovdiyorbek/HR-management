package org.example.organization.controller

import org.example.organization.dto.EmployeeCreateRequest
import org.example.organization.dto.EmployeeResponse
import org.example.organization.dto.EmployeeUpdateRequest
import org.example.organization.service.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizations/{organizationId}/employees")
class EmployeeController(
    private val service: EmployeeService
) {

    @PostMapping
    fun addEmployee(
        @PathVariable organizationId: Long,
        @RequestBody body: EmployeeCreateRequest,
        @RequestParam(required = false) createdByUserId: Long?
    ): ResponseEntity<Unit> {
        service.addEmployee(organizationId, body, createdByUserId)
        return ResponseEntity.ok().build()
    }

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
}

@RestController
@RequestMapping("/api/users")
class UserOrganizationsController(
    private val service: EmployeeService
) {

    @GetMapping("/{userId}/organizations")
    fun getMyOrganizations(@PathVariable userId: Long): List<Long> =
        service.getMyOrganizations(userId)
}