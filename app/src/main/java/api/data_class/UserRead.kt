package api.data_class

data class UserRead(
    val userId: Int, // Changed from id
    val username: String,
    val email: String,
    val createdAt: String // Add this field, consider using a Date type if needed
)
