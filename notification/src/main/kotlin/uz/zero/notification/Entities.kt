package uz.zero.notification

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
    var url: String,
    var isUsed: Boolean = false
) : BaseEntity()

@Entity
@Table(name = "notification")
class Notification(
    var companyId: Long,
    var companyName: String,
    var projectId: Long,
    var projectName: String,
    var taskId: Long,
    var taskName: String,
    var actionType: ActionType,
    var actionByName: String,
    var actionById: Long,
    var message: String,
    var fromTaskId: Long? = null,
    var toTaskId: Long? = null,
    var url: String,
) : BaseEntity()

@Entity
@Table(name = "user_notification")
class UserNotification(
    @ManyToOne var userTelegram: UserTelegram,
    @ManyToOne var notification: Notification,
    @Column(nullable = false) @ColumnDefault(value = "false") var isSend: Boolean
) : BaseEntity()