package api.data_class

data class BudgetRead(
    val budgetId: Int,
    val userId: Int,
    val budgetCategoryId: Int,
    val amount: Double,
    val startDate: String,
    val endDate: String,
)
