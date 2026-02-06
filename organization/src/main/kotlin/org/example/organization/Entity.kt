package org.example.organization

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var lastModifiedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false // (true - o'chirilgan bo'lsa)  (false - o'chirilmagan)
)

@Entity
@Table(name = "organizations")
class Organization(
    @Column(unique = true, nullable = false)
    var name: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    // auth-service dagi user ID
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null
) : BaseEntity()

@Entity
@Table(
    name = "employees",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_employees_user_org",
            columnNames = ["user_id", "organization_id"]
        )
    ]
)
class Employee(
    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false)
    var employeeRole: EmployeeRole,

    @Column(nullable = true)
    var department: String? = null,

    @Column(nullable = true)
    var position: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null
) : BaseEntity()

@Entity
@Table(
    name = "employee_context",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_employee_context_user", columnNames = ["user_id"])
    ]
)
class EmployeeContext(
    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_organization_id", nullable = false)
    var currentOrganization: Organization,

    @Column(name = "last_accessed_at", nullable = false)
    var lastAccessedAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity()