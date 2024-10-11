package api.data_class

data class UserCreate(
    val username: String,
    val email: String,
    val hashed_password: String
)