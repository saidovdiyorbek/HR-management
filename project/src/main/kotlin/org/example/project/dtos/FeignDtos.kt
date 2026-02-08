package org.example.project.dtos

import org.example.project.Permission

data class CurrentUserOrganizationDto(
    val organizationId: Long,
)

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
)

data class TransferTaskCheckDto(
    val fromStateId: Long,
    val toStateId: Long,
    val boardId: Long,
    val permission: Permission,
)
