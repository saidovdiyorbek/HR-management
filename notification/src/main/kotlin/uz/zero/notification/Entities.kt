package uz.zero.notification

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
import org.hibernate.annotations.CreationTimestamp
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var lastModifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
@Table(name = "user_telegram")
class UserTelegram(
    @Column(unique = true) var chatId: Long,
    var firstName: String? = null,
    var lastName: String? = null,
    var username: String? = null,
    var userId: Long
) : BaseEntity()


@Entity
@Table(name = "hash")
class Hash(
    @Column(unique = true, nullable = false) var hash: String,
    var userId: Long,
    @Column(nullable = false) var expriTime: LocalDateTime,
    @Column(columnDefinition = "TEXT")
    var url: String,
    var isUsed: Boolean = false
) : BaseEntity()

@Entity
@Table(name = "notification")
class Notification(
    var companyId: Long,
    var companyName: String,
    var projectId: Long?,
    var projectName: String,
    var taskId: Long?,
    @Column(columnDefinition = "TEXT")
    var taskName: String? = null,
    @Enumerated(EnumType.STRING)
    var actionType: ActionType,
    @Column(columnDefinition = "TEXT")
    var message: String? = null,
    var fromTaskId: Long? = null,
    @Column(columnDefinition = "TEXT")
    var fromTaskName: String? = null,
    var toTaskId: Long? = null,
    @Column(columnDefinition = "TEXT")
    var toTaskName: String? = null,
    @Column(columnDefinition = "TEXT")
    var url: String? = null,
) : BaseEntity()

@Entity
@Table(name = "user_notification")
class UserNotification(
    @ManyToOne var userTelegram: UserTelegram,
    @ManyToOne var notification: Notification,
    @Enumerated(EnumType.STRING)
    var status: NotificationStatus = NotificationStatus.PENDING
) : BaseEntity()

/*@Entity
class TaskAction(
    val taskId: Long,
    val userId: Long,
    @Enumerated(EnumType.STRING)
    val type: ActionType,
    @Column(columnDefinition = "TEXT")
    val details: String? = null,
) : BaseEntity()*/

@Entity
@Table(name = "notification_logs")
data class NotificationLog(

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val taskId: Long,

    @Column(columnDefinition = "TEXT")
    val message: String,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,
) : BaseEntity()