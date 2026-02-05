package org.example.project.dtos

data class  BoardTaskStateCreateDto(
    val boardId : Long,
    val taskStateId : Long,
    val position: Int
)
