package api.data_class

data class ProfileCreate(
    val user_id: Int,
    val first_name: String,
    val last_name: String,
    val phone_number: String
)