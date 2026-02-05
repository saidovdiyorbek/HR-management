package org.example.project

import org.example.project.dtos.CurrentUserOrganizationDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "organization-service", url = "\${organization.service.host}")
interface OrganizationClient {
    @GetMapping("/organization/current/{id}")
    fun getCurrentUserOrganization(@PathVariable id:Long): CurrentUserOrganizationDto
}

