package api.data_class

data class UserRead(
    val userId: Int,
    val username: String,
    val email: String,
    val createdAt: String
)
