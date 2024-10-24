package api.data_class

data class BudgetCreate(
    val userId: Int,
    val categoryId: Int,
    val amount: Double,
    val startDate: String,
    val endDate: String,
)
