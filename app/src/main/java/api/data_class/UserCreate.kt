package api.data_class

data class UserCreate(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String,
    val phone_number: String
)
