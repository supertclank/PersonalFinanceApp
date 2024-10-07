package api.data_class

data class ProfileCreate(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String
)
