package api.data_class

data class ReportRead(
    val id: Int,
    val user_id: Int,
    val report_type_id: Int,
    val generated_at: String,
    val data: Map<String, Any>,
)
