package org.example.project

import jakarta.persistence.EntityManager
import org.example.project.dtos.StateShortInfoDto
import org.example.project.dtos.TaskStateWithPositionDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    entityManager: EntityManager,
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    private val isNotDeletedSpecification = Specification<T> { root, _, cb ->
        cb.equal(root.get<Boolean>("deleted"), false)
    }

    override fun findByIdAndDeletedFalse(id: Long): T? =
        findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? =
        findByIdOrNull(id)?.run {
            deleted = true
            save(this)
        }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)

    override fun findAllNotDeleted(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}

@Repository
interface ProjectRepository : BaseRepository<Project> {
    fun findAllByOrganizationIdAndDeletedFalse(organizationId: Long, pageable: Pageable) : Page<Project>
}

@Repository
interface BoardRepository : BaseRepository<Board> {
    fun findByProjectIdAndDeletedFalse(id: Long): List<Board>
    fun existsByNameAndProjectAndDeletedIsFalse(name: String, project: Project): Boolean
}

@Repository
interface TaskStateRepository : BaseRepository<TaskState> {
    fun findByOrganizationIdAndDeletedFalse(organizationId: Long, pageable: Pageable) : Page<TaskState>

    @Query(
        """
        SELECT NEW org.example.project.dtos.TaskStateWithPositionDto(ts.id, ts.permission, bt.position)
        FROM TaskState ts 
        JOIN BoardTaskState bt ON ts.id = bt.taskState.id 
        WHERE ts.id = :stateId AND bt.board.id = :boardId AND ts.deleted = false AND bt.deleted = false
        """
    )
    fun findTaskStateWithPosition(stateId: Long, boardId: Long) : TaskStateWithPositionDto?

    @Query(
        """
        SELECT ts 
        FROM TaskState ts 
        JOIN BoardTaskState bt ON ts.id = bt.taskState.id 
        WHERE bt.board.id = :boardId AND ts.deleted = false AND bt.deleted = false
        """
    )
    fun findAllByBoardIdAndDeletedFalse(boardId: Long, pageable: Pageable) : Page<TaskState>
    fun findAllByOrganizationIdAndDeletedFalse(organizationId: Long, pageable: Pageable) : Page<TaskState>
    fun existsByNameAndOrganizationIdAndDeletedFalse(name: String, organizationId: Long): Boolean
}

@Repository
interface BoardTaskStateRepository : BaseRepository<BoardTaskState> {
    fun findByBoardIdAndDeletedFalse(boardId: Long): List<BoardTaskState>
    
    @Query("SELECT MAX(bts.position) FROM BoardTaskState bts WHERE bts.board.id = :boardId AND bts.deleted = false")
    fun findMaxPosition(boardId: Long): Int?
    fun existsByBoardAndTaskStateAndDeletedFalse(board: Board, taskState: TaskState): Boolean
    @Query(
        """
        SELECT NEW org.example.project.dtos.StateShortInfoDto(ts.id, ts.name, bt.position)
        FROM TaskState ts 
        JOIN BoardTaskState bt ON ts.id = bt.taskState.id 
        WHERE bt.board.id = :boardId AND ts.deleted = false AND bt.deleted = false
        ORDER BY bt.position ASC
        """
    )
    fun findByBoardIdWithStateAndDeletedFalse(boardId: Long) :List<StateShortInfoDto>
}

@Repository
interface TaskStateTemplateRepository : BaseRepository<TaskStateTemplate> {
    fun findAllByOrganizationIdAndDeletedFalse(organizationId: Long): List<TaskStateTemplate>
    fun existsByNameAndDeletedFalse(name: String): Boolean
}

@Repository
interface TaskStateTemplateItemRepository : BaseRepository<TaskStateTemplateItem> {
    fun findAllByTemplateIdAndDeletedFalse(templateId: Long): List<TaskStateTemplateItem>?
}

@Repository
interface BoardUserRepository : BaseRepository<BoardUser> {
    fun existsByBoardIdAndUserIdAndDeletedFalse(boardId: Long, userId: Long): Boolean
    fun findByBoardIdAndDeletedFalse(id: Long) : List<BoardUser>

}