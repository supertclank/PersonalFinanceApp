package api.data_class

data class TransactionCreate(
    val userId: Int,
    val amount: Double,
    val categoryId: Int,
    val date: String,
    val description: String,
)
