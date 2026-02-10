package org.example.organization

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
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

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeletedForPageable(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeletedForPageable(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}


@Repository
interface OrganizationRepository : BaseRepository<Organization> {

    fun findByNameIgnoreCase(name: String): Organization?

    fun existsByNameIgnoreCase(name: String): Boolean

    fun findAllByDeletedFalse(): List<Organization>
    fun findAllByCreatedByUserIdAndDeletedFalse(userId: Long): List<Organization>
}


@Repository
interface EmployeeRepository : BaseRepository<Employee> {

    fun existsByUserIdAndOrganizationId(userId: Long, organizationId: Long): Boolean

    fun findByUserIdAndOrganizationIdAndDeletedFalse(userId: Long, organizationId: Long): Employee?

    fun findAllByUserIdAndDeletedFalse(userId: Long): List<Employee>

    fun findAllByOrganizationIdAndDeletedFalse(organizationId: Long): List<Employee>

    fun findByOrganizationIdAndEmployeeRoleAndDeletedFalse(
        organizationId: Long,
        employeeRole: EmployeeRole
    ): Employee?

    @Query("""
    SELECT COUNT(e) 
    FROM Employee e
    WHERE e.organization.id = :organizationId
      AND e.userId IN :userIds
      AND e.deleted = false
""")
    fun countEmployeesInOrganization(
        organizationId: Long,
        userIds: List<Long>
    ): Long


}


@Repository
interface EmployeeContextRepository : BaseRepository<EmployeeContext> {

    @Query("""
        select ec from EmployeeContext ec 
        where ec.userId = ?1 and ec.deleted = false
    """)
    fun findEmployeeContextByUserId(userId: Long): EmployeeContext?

    fun existsByUserIdAndDeletedFalse(userId: Long): Boolean
    fun existsEmployeeContextByUserId(userId: Long): Boolean
}
