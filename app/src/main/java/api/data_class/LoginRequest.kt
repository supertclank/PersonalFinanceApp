package api.data_class

data class LoginRequest(
    val username: String,
    val hashed_password: String
)