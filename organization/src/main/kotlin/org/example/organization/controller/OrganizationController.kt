package org.example.organization.controller

import org.example.organization.dto.OrganizationAllResponse
import org.example.organization.dto.OrganizationCreateRequest
import org.example.organization.dto.OrganizationFullResponse
import org.example.organization.dto.OrganizationUpdateRequest
import org.example.organization.service.OrganizationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    private val service: OrganizationService
) {

    @PostMapping
    fun create(
        @RequestBody body: OrganizationCreateRequest,
        @RequestParam(required = false) createdByUserId: Long?
    ): ResponseEntity<Unit> {
        service.create(body, createdByUserId)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getAll(): List<OrganizationAllResponse> =
        service.getAll()

    @GetMapping("/page")
    fun getAllPaginated(pageable: Pageable): Page<OrganizationAllResponse> =
        service.getAllPaginated(pageable)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): OrganizationFullResponse =
        service.getOne(id)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody body: OrganizationUpdateRequest
    ): ResponseEntity<Unit> {
        service.update(id, body)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.ok().build()
    }
}
