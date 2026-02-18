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
import org.hibernate.annotations.CreationTimestamp
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
    @CreatedDate @Temporal(TemporalType.TIMESTAMP)var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP)var lastModifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
@Table(name = "user_telegram")
class UserTelegram(
    @Column(unique = true)var chatId: Long,
    var firstName: String? = null,
    var lastName: String? = null,
    var username: String? = null
) : BaseEntity()


@Entity
@Table(name = "hash")
class Hash(
    @Column(unique = true, nullable = false) var hash: UUID,
    @Column (nullable = false) var expriTime: LocalDateTime,
) : BaseEntity()

@Entity
class TaskAction(
    val taskId: Long,
    val userId: Long,
    @Enumerated(EnumType.STRING)
    val type: ActionType,
    @Column(columnDefinition = "TEXT")
    val details: String? = null,
) : BaseEntity()

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