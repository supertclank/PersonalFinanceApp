package api.data_class

data class GoalsCreate(
    val name: String,
    val target_amount: Double,
    val current_amount: Double,
    val deadline: String,
    val description: String?
)