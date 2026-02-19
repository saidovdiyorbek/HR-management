package uz.zero.notification

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import uz.zero.notification.dtos.CurrentOrganizationResponse
import uz.zero.notification.dtos.OrganizationInfo
import uz.zero.notification.dtos.ProjectShortInfo
import uz.zero.notification.dtos.SendMessageRequest
import uz.zero.notification.dtos.UserShortInfo

@FeignClient(name = "telegram-client", url = "\${telegram.bot.url}")
interface TelegramFeignClient {

    @PostMapping("/sendMessage")
    fun sendMessage(@RequestBody request: SendMessageRequest)
}

@FeignClient(name = "auth-service", url = "\${services.hosts.auth}/internal", configuration = [FeignOAuth2TokenConfig::class])
interface AuthUserClient {

    @GetMapping("/check-user/{userId}")
    fun exists(@PathVariable userId: Long): Role

    @GetMapping("user-short-info/{userId}")
    fun getUserShortInfo(@PathVariable userId: Long): UserShortInfo

}

@FeignClient(name = "employee-service", url = "\${services.hosts.organization}/internal/employee-context", configuration = [FeignOAuth2TokenConfig::class])
interface EmployeeContextClient {

    @GetMapping("/get-current-organization/{userId}")
    fun getCurrentOrganizationByUserId(
        @PathVariable userId: Long
    ): CurrentOrganizationResponse
}

@FeignClient(name = "organization-service", url = "\${services.hosts.organization}/internal/api/v1/organizations", configuration = [FeignOAuth2TokenConfig::class])
interface OrganizationClient {

    @GetMapping("/get-organization-info/{userId}")
    fun getOrganizationInfo(@PathVariable userId: Long): OrganizationInfo
}

@FeignClient(name = "project-service", url = "\${services.hosts.project}/internal/api/v1/projects" , configuration = [FeignOAuth2TokenConfig::class])
interface ProjectClient {

    @GetMapping("/get-project/{boardId}")
    fun getProjectShortInfoByBoardId(@PathVariable boardId: Long) : ProjectShortInfo
}