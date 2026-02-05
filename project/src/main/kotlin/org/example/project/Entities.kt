package org.example.project

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate:  LocalDateTime? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate:  LocalDateTime? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var lastModifiedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity
@Table(name = "project")
class Project(
    @Column(nullable = false) var name: String,
    @Column(columnDefinition = "TEXT") var description: String?,
    @Column(nullable = false, name = "organization_id") var organizationId: Long,
    @Column(nullable = false, name = "is_active") val isActive: Boolean=true,
    @Column(name = "start_date") val startDate: LocalDate,
    @Column(name = "end_date") val endDate: LocalDate?
) : BaseEntity()


@Entity
@Table(name = "board")
class Board(
    @Column(nullable = false) var name: String,
    @Column(columnDefinition = "TEXT") var description: String?,
    @ManyToOne val project: Project
) : BaseEntity()

@Entity
@Table(name = "task_state")
class TaskState(
    @Column(nullable = false) var name: String,
    @Column(columnDefinition = "TEXT") var description: String?,
    @Enumerated(value = EnumType.STRING)
    @Column(length = 10) var permission: Permission,
    @Column(nullable = false, name = "organization_id") val organizationId: Long
):BaseEntity()

@Entity
@Table(name = "board_task_state")
class BoardTaskState(
    @ManyToOne val board: Board,
    @ManyToOne val taskState: TaskState,
    @Column(nullable = false) var position: Int
):BaseEntity()

