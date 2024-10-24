package api.data_class

data class ReportCreate(
    val userId: Int,
    val reportTypeId: Int,
    val data: Map<String, Any>,
)
