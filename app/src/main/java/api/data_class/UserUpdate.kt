package api.data_class

data class UserUpdate(
    val username: String,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val email: String,
)