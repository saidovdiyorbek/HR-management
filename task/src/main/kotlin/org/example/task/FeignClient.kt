package org.example.task

import org.example.task.dtos.BoardInfoDto
import org.example.task.dtos.CheckResponse
import org.example.task.dtos.CheckUsersInOrganizationRequest
import org.example.task.dtos.CurrentOrganizationResponse
import org.example.task.dtos.EmployeeRoleResponse
import org.example.task.dtos.InternalHashesCheckRequest
import org.example.task.dtos.RelationshipsCheckDto
import org.example.task.dtos.RequestEmployeeRole
import org.example.task.dtos.TransferTaskCheckDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "project-service", url = "\${services.hosts.project}/internal/api/v1/projects", configuration = [FeignOAuth2TokenConfig::class])
interface ProjectClient{
    @PostMapping("/check-relationships")
    fun checkTaskRelationships(@RequestBody body: RelationshipsCheckDto): CheckResponse

    @PostMapping("/check-state-relationships")
    fun checkTransferStates(@RequestBody body: TransferTaskCheckDto): Boolean

    @GetMapping("/get-board-users/{boardId}")
    fun getBoardUsers(@PathVariable boardId: Long): BoardInfoDto
}

@FeignClient(name = "attach-service", url = "\${services.hosts.attach}/internal/api/v1/attaches", configuration = [FeignOAuth2TokenConfig::class])
interface AttachClient{
    @PostMapping("/hashes/exists")
    fun listExists(@RequestBody hashes: InternalHashesCheckRequest): Boolean

    @DeleteMapping("/deleteList")
    fun deleteList(@RequestBody hashes: List<String>)
}

@FeignClient(name = "organization-service", url = "\${services.hosts.organization}/internal/api/v1/employee-context", configuration = [FeignOAuth2TokenConfig::class])
interface OrganizationClient{

    @GetMapping("/get-current-organization/{userId}")
    fun getCurrentOrganizationByUserId(
        @PathVariable userId: Long
    ): CurrentOrganizationResponse
}


@FeignClient(name = "employee-service", url = "\${services.hosts.organization}/internal/api/v1/employees", configuration = [FeignOAuth2TokenConfig::class])
interface EmployeeClient{
    @PostMapping("/check-users-in-organization")
    fun checkUsersInOrganization(
        @RequestBody dto: CheckUsersInOrganizationRequest
    ): Boolean

    @PostMapping("/get-employee-role/{userId}")
    fun getEmployeeRole(@PathVariable userId: Long, @RequestBody dto: RequestEmployeeRole): EmployeeRoleResponse


}