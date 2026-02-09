package org.example.project

import org.example.project.dtos.CurrentUserOrganizationDto
import org.example.project.dtos.EmployeeRoleResponse
import org.example.project.dtos.RequestEmployeeRole
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "organization-service", url = "\${services.hosts.organization}/internal/api/v1/employee-context" , configuration = [FeignOAuth2TokenConfig::class])
interface OrganizationClient {
    @GetMapping("/get-current-organization/{userId}")
    fun getCurrentUserOrganization(@PathVariable userId:Long): CurrentUserOrganizationDto

}
@FeignClient(name = "employee-service", url = "\${services.hosts.organization}/internal/api/v1/employees" , configuration = [FeignOAuth2TokenConfig::class])
interface EmployeeClient {

    @PostMapping("/get-employee-role/{userId}")
    fun getEmployeeRoleByUserId(@PathVariable userId: Long, @RequestBody dto: RequestEmployeeRole):EmployeeRoleResponse
}



