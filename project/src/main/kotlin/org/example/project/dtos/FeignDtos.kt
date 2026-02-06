package org.example.project.dtos

data class CurrentUserOrganizationDto(
    val id: Long,
)

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
)