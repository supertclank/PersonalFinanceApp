package api.data_class

data class NotificationCreate(
    val userId: Int,
    val message: String,
    val notificationTypeId: Int,
    val isRead: Boolean
)
