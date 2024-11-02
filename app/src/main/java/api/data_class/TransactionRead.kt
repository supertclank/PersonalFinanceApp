package api.data_class

data class TransactionRead(
    val id: Int,
    val user_id: Int,
    val amount: Double,
    val transaction_category_id: Int,
    val date: String,
    val description: String,
)
