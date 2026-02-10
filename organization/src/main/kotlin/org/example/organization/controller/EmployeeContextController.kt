package org.example.organization.controller

import org.example.organization.dto.CurrentOrganizationResponse
import org.example.organization.dto.SetCurrentOrganizationRequest
import org.example.organization.service.EmployeeContextService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/organizations/employee-context")
class EmployeeContextController(
    private val service: EmployeeContextService
) {

    @PutMapping("/current/{userId}")
    fun setCurrentOrganization(
        @PathVariable userId: Long,
        @RequestBody body: SetCurrentOrganizationRequest
    ): ResponseEntity<Unit> {
        service.setCurrentOrganization(userId, body)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/current/{userId}")
    fun getCurrentOrganization(
        @PathVariable userId: Long
    ): CurrentOrganizationResponse =
        service.getCurrentOrganization(userId)
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