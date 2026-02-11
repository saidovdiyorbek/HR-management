package org.example.organization

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "auth-service", url = "\${services.hosts.auth}/internal", configuration = [FeignOAuth2TokenConfig::class])
interface AuthUserClient {

    @GetMapping("/check-user/{userId}")
    fun exists(@PathVariable userId: Long): Role

}
