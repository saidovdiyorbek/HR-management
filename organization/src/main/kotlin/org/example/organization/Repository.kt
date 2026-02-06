package org.example.organization

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
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

    /** Organization nomi bo'yicha topish (duplicate tekshiruv uchun) */
    fun findByNameIgnoreCase(name: String): Organization?

    /** Organization nomi bo‘yicha mavjudligini tekshiradi (create/update paytida) */
    fun existsByNameIgnoreCase(name: String): Boolean

    /** Soft-delete bo‘lmagan orglarni qaytaradi (BaseRepository’dagi findAllNotDeleted bilan ham bo‘ladi) */
    fun findAllByDeletedFalse(): List<Organization>
}


@Repository
interface EmployeeRepository : BaseRepository<Employee> {

    /** Bitta user bitta org’da allaqachon membership bormi (duplicate’ni oldini oladi) */
    fun existsByUserIdAndOrganizationId(userId: Long, organizationId: Long): Boolean

    /** Userning ma’lum org’dagi membership’ini topadi (role tekshirish, context set uchun) */
    fun findByUserIdAndOrganizationIdAndDeletedFalse(userId: Long, organizationId: Long): Employee?

    /** User qaysi organizationlarda ishlashini chiqaradi (My organizations) */
    fun findAllByUserIdAndDeletedFalse(userId: Long): List<Employee>

    /** Organization ichidagi employee’larni chiqaradi (admin panel / employee list) */
    fun findAllByOrganizationIdAndDeletedFalse(organizationId: Long): List<Employee>

    /** Organization ichida CEO kimligini topish (agar 1 ta CEO bo‘lishi kerak bo‘lsa) */
    fun findByOrganizationIdAndEmployeeRoleAndDeletedFalse(
        organizationId: Long,
        employeeRole: EmployeeRole
    ): Employee?
}


@Repository
interface EmployeeContextRepository : BaseRepository<EmployeeContext> {

    /** Userning current organization context’ini topadi (qaysi org tanlangan) */
    fun findByUserIdAndDeletedFalse(userId: Long): EmployeeContext?

    /** Userda context mavjudligini tekshiradi (create vs update uchun) */
    fun existsByUserIdAndDeletedFalse(userId: Long): Boolean
}

