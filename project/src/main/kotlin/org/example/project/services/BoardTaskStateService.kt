package org.example.project.services

import org.example.project.BoardNotFoundException
import org.example.project.BoardRepository
import org.example.project.BoardTaskStateMapper
import org.example.project.BoardTaskStateRepository
import org.example.project.TaskStateNotFoundException
import org.example.project.TaskStateRepository
import org.example.project.dtos.BoardTaskStateCreateDto
import org.springframework.stereotype.Service

interface BoardTaskStateService {
    fun create(dto: BoardTaskStateCreateDto)
    fun delete(id: Long)
}

@Service
class BoardTaskStateServiceImpl(
    private val repository: BoardTaskStateRepository,
    private val mapper: BoardTaskStateMapper,
    private val boardRepository: BoardRepository,
    private val taskStateRepository: TaskStateRepository
) : BoardTaskStateService {
    override fun create(dto: BoardTaskStateCreateDto) {
        val board = boardRepository.findByIdAndDeletedFalse(dto.boardId)
            ?: throw BoardNotFoundException()
        val taskState = taskStateRepository.findByIdAndDeletedFalse(dto.taskStateId)
            ?: throw TaskStateNotFoundException()
        val boardTaskState = mapper.toEntity(dto, board, taskState)
        repository.save(boardTaskState)
    }

    override fun delete(id: Long) {
        repository.trash(id)
    }
}
