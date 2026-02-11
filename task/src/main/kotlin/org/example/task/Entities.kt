package org.example.task

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP)var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP)var lastModifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)
@Entity
class Task(
    var boardId: Long,
    var stateId: Long,
    var createUserId: Long,
    @Column(unique = true) var taskNumber: String,
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    var priority: TaskPriority,
    var estimatedHours: Double? = null,
    var deadline: Date? = null,
    var orderIndex: Int,
    var tags: List<String>? = null,
    var isActive: Boolean = true,
    val completedAt: Date? = null,
    val currentOrganizationId: Long,
) : BaseEntity()

@Entity
class TaskAttachment(
    @ManyToOne
    val task: Task,
    val fileHash: String,
) : BaseEntity()

@Entity
class TaskHistory(
    @ManyToOne
    val task: Task,
    val fromStateId: Long,
    val toStateId: Long,
    val changedByEmployeeId: Long,
    @Column(columnDefinition = "TEXT")
    val comments: String,
) : BaseEntity()

@Entity
class TaskLabel(
    val boardId: Long,
    val name: String,
    val color: String,
    val isActive: Boolean,
) : BaseEntity()

@Entity
class TaskLabelMapping(
    @ManyToOne
    val task: Task,
    @ManyToOne
    val label: TaskLabel,
) : BaseEntity()

@Entity
class TaskAssignedEmployee(
    @ManyToOne
    val task: Task,
    val employeeId: Long,
    val assignedBy: Long,
) : BaseEntity()