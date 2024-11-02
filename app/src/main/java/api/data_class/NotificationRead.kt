package api.data_class

data class NotificationRead(
    val id: Int,
    val user_id: Int,
    val message: String,
    val notification_type_id: Int,
    val isRead: Boolean,
    val created_at: String,
)
