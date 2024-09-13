package api.data_class

data class GoalsCreate(
    val userId: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val description: String
)
