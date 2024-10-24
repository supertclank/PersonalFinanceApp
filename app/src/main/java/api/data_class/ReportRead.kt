package api.data_class

data class ReportRead(
    val id: Int,
    val userId: Int,
    val reportTypeId: Int,
    val generatedAt: String,
    val data: Map<String, Any>,
)
