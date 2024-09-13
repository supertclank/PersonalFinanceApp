package api.data_class

data class NotificationRead(
    val id: Int,
    val userId: Int,
    val message: String,
    val notificationTypeId: Int,
    val isRead: Boolean,
    val createdAt: String
)
