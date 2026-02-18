package uz.zero.notification

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.notification.services.HashService

@RestController
@RequestMapping("/notification")
class HashController(
    val hashService: HashService
) {

    @PostMapping("/hash")
    fun createHash(): String = hashService.generateHash()

}