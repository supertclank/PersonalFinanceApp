package api.data_class

data class ProfileCreate(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String
)
