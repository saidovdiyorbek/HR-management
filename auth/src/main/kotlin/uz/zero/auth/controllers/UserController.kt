package uz.zero.auth.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.auth.model.requests.PasswordUpdateDto
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.requests.UserUpdateRequest
import uz.zero.auth.services.UserService

@RestController
@RequestMapping("user")
class UserController(
    private val userService: UserService
){
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @PostMapping("/register")
    fun registerUser(@RequestBody request: UserCreateRequest) = userService.registerUser(request)

    @GetMapping("/me")
    fun userMe() = userService.userMe()

    @PutMapping("/{id}")
    fun updateUser(@RequestBody request: UserUpdateRequest, @PathVariable id: Long) = userService.update(id,request)

    @PutMapping("/password/{id}")
    fun updatePassword(@RequestBody request: PasswordUpdateDto, @PathVariable id: Long) = userService.updatePassword(id,request)

    @GetMapping("/test")
    fun testAdd() = "test adding "
}