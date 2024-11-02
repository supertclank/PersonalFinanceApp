package api.data_class

data class BudgetCreate(
    val user_id: Int,
    val budget_category_id: Int,
    val amount: Double,
    val start_date: String,
    val end_date: String,
)
