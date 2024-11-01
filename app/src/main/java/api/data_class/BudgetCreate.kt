package api.data_class

data class BudgetCreate(
    val userId: Int,
    val budgetCategoryId: Int,
    val amount: Double,
    val startDate: String,
    val endDate: String,
)
