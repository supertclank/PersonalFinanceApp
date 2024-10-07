package api.data_class

data class ProfileRead(
    val profileId: Int,
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val createdAt: String
)
