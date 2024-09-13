package api.data_class

data class ProfileRead(
    val profileId: Int, // Changed from id
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val createdAt: String // Add this field, consider using a Date type if needed
)
