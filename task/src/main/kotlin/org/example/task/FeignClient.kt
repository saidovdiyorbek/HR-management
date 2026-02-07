package org.example.task

import org.example.task.dtos.InternalHashesCheckRequest
import org.example.task.dtos.RelationshipsCheckDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "project-service", url = "\${services.hosts.project}/internal/api/v1/projects", configuration = [FeignOAuth2TokenConfig::class])
interface ProjectClient{
    @PostMapping("/check-relationships")
    fun checkTaskRelationships(@RequestBody body: RelationshipsCheckDto): Boolean
}

@FeignClient(name = "attach-service", url = "\${services.hosts.attach}/internal/api/v1/attaches", configuration = [FeignOAuth2TokenConfig::class])
interface AttachClient{
    @PostMapping("/hashes/exists")
    fun listExists(@RequestBody hashes: InternalHashesCheckRequest): Boolean
}