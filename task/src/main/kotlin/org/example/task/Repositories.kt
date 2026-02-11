package org.example.task

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun findByIdFull(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    entityManager: EntityManager
): SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {
    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), true) }
    override fun findByIdAndDeletedFalse(id: Long): T? = findByIdOrNull(id)?.run { if (deleted) null else this }
    override fun findByIdFull(id: Long): T? = findByIdOrNull(id)

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)

    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)

}

interface TaskRepository : BaseRepository<Task>{
    @Query("""
        select max(t.orderIndex) from Task t
    """)
    fun getTaskLastOrderIndex(): Int?

    @Query("""
        select t from Task t
        where t.currentOrganizationId = :organizationId and t.createUserId = :userId and t.deleted = false
    """)
    fun getEmployeeTaskCurrentOrganization(organizationId: Long, userId: Long, pageable: Pageable): Page<Task>

    @Modifying
    @Query("""update Task t set t.orderIndex = ?2
        where t.id = ?1
    """)
    fun updateTaskOrderIndex(taskId: Long, orderIndex: Int)

    @Modifying
    @Query("""update Task t set t.orderIndex = t.orderIndex + 1
        where t.orderIndex >= :orderIndex and t.orderIndex < :oldIndex 
    """)
    fun updateTaskOrderIndexesIncrement(orderIndex: Int, oldIndex: Int)

    @Modifying
    @Query("""update Task t set t.orderIndex = t.orderIndex - 1
        where t.orderIndex < :newIndex and t.orderIndex > :oldIndex 
    """)
    fun updateTaskOrderIndexesDecrement(newIndex: Int,  oldInex: Int)


}
interface TaskAttachmentRepository : BaseRepository<TaskAttachment>{
    @Query("""
        select ta.fileHash from TaskAttachment ta
        where ta.task.id = :taskId and ta.deleted = false 
    """)
    fun findTaskAttachmentByTaskId(taskId: Long): List<String>

    @Query("""select ta.fileHash  from TaskAttachment ta 
        where ta.task.id = : id and ta.deleted = false
    """)
    fun getPostAttachHash(id: Long): List<String>

    @Modifying
    @Query("""
        delete from TaskAttachment ta
        where ta.fileHash in ?1
    """)
    fun removeByFileHashList(hashesToRemove: List<String>)
}
interface TaskHistoryRepository : BaseRepository<TaskHistory>{}
interface TaskLabelRepository : BaseRepository<TaskLabel>{}
interface TaskLabelMappingRepository : BaseRepository<TaskLabelMapping>{}
interface TaskAssignedEmployeeRepository : BaseRepository<TaskAssignedEmployee>{

    fun existsTaskAssignedEmployeeByTaskId(taskId: Long): Boolean

    @Query("""
        select ta.employeeId from TaskAssignedEmployee ta
        where ta.task.id = :taskId and ta.deleted = false
    """)
    fun findTaskAssignedEmployeeByTaskId(taskId: Long): List<Long>

    @Modifying
    @Query("""
        update TaskAssignedEmployee t set t.deleted = true
        where t.employeeId in (:employeeIds)
    """)
    fun deleteTaskAssignedEmployeeByEmployeeIds(employeeIds: List<Long>)
}