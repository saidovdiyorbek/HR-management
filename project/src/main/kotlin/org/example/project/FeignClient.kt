package org.example.project

import org.example.project.dtos.CurrentUserOrganizationDto
import org.example.project.dtos.EmployeeRoleResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "organization-service", url = "\${services.hosts.organization}/internal/api/v1/employee-context" , configuration = [FeignOAuth2TokenConfig::class])
interface OrganizationClient {
    @GetMapping("/get-current-organization/{userId}")
    fun getCurrentUserOrganization(@PathVariable userId:Long): CurrentUserOrganizationDto

    @GetMapping("/get-employee-role/{userId}")
    fun getEmployeeRoleByUserId(@PathVariable userId: Long): EmployeeRoleResponse
}



