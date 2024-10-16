package api.data_class

data class ProfileRead(
    val id: Int,
    val user_id: Int,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val created_at: String // Adjust type as necessary
)