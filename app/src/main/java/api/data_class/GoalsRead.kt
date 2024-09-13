package api.data_class

data class GoalsRead(
    val id: Int,
    val userId: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val description: String
)
