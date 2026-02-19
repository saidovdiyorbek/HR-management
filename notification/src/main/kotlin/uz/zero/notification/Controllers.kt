package uz.zero.notification

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class HashController(
    val hashService: HashService
) {

    @PostMapping("/hash")
    fun createHash(): String = hashService.generateHash()

}