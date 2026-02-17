package uz.zero.notification

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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