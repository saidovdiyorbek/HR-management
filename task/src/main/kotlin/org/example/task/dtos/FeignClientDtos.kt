package org.example.task.dtos

data class RelationshipsCheckDto(
    val boardId: Long,
    val stateId: Long,
)
data class InternalHashesCheckRequest(
    val userId: Long,
    val hashes: List<String>
)