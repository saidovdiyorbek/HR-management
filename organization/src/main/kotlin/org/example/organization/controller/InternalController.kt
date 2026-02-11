package org.example.organization.controller

import org.example.organization.dto.CheckUsersInOrganizationRequest
import org.example.organization.dto.CurrentOrganizationResponse
import org.example.organization.dto.EmployeeRoleResponse
import org.example.organization.dto.RequestEmployeeRole
import org.example.organization.service.EmployeeContextService
import org.example.organization.service.EmployeeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/api/v1/employees")
class EmployeeInternalController(
    private val service: EmployeeService
) {
    @PostMapping("/get-employee-role/{userId}")
    fun getEmployeeRole(@PathVariable userId: Long, @RequestBody dto: RequestEmployeeRole): EmployeeRoleResponse = service.getEmployeeRole(dto)

    @PostMapping("/check-users-in-organization")
    fun checkUsersInOrganization(
        @RequestBody dto: CheckUsersInOrganizationRequest
    ): Boolean = service.areAllUsersInOrganization(dto.organizationId, dto.userIds)
}

@RestController
@RequestMapping("/internal/api/v1/employee-context")
class EmployeeContextInternalController(
    private val service: EmployeeContextService
) {

    @GetMapping("/get-current-organization/{userId}")
    fun getCurrentOrganizationByUserId(
        @PathVariable userId: Long
    ): CurrentOrganizationResponse = service.getCurrentOrganization(userId)
}