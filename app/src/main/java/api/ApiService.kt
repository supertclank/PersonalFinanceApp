package api

import api.data_class.BudgetCategory
import api.data_class.BudgetCreate
import api.data_class.BudgetRead
import api.data_class.GoalsCreate
import api.data_class.GoalsRead
import api.data_class.NotificationCreate
import api.data_class.NotificationRead
import api.data_class.NotificationType
import api.data_class.ReportCreate
import api.data_class.ReportRead
import api.data_class.ReportType
import api.data_class.TokenResponse
import api.data_class.TransactionCategory
import api.data_class.TransactionCreate
import api.data_class.TransactionRead
import api.data_class.UserCreate
import api.data_class.UserRead
import api.data_class.UsernameRecoveryRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("users/email/{email}")
    fun checkUserExistsByEmail(@Path("email") email: String): Call<Boolean>

    // User endpoints
    @POST("users/")
    fun createUser(@Body user: UserCreate): Call<UserRead>

    @GET("users/")
    fun getUsers(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<UserRead>>

    @GET("user/{user_id}")
    fun getUser(@Path("user_id") userId: Int): Call<UserRead>

    @GET("user/username/{username}")
    fun getUserByUsername(@Path("username") username: String): Call<UserRead>

    @FormUrlEncoded
    @POST("login/")
    fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String? = null,
        @Field("client_id") clientId: String? = null,
        @Field("client_secret") clientSecret: String? = null,
    ): Call<TokenResponse>

    // Budget endpoints
    @POST("budgets/")
    fun createBudget(
        @Body newBudget: BudgetCreate,
        @Header("Authorization") token: String,
    ): Call<BudgetRead>

    @GET("budgets/")
    fun getBudgets(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String,
    ): Call<List<BudgetRead>>

    @GET("budgets/{budgetId}")
    fun getBudget(
        @Path("budgetId") budgetId: Int,
        @Header("Authorization") token: String,
    ): Call<BudgetRead>

    @PUT("budgets/{budgetId}")
    fun updateBudget(
        @Path("budgetId") budgetId: Int,
        @Body budget: BudgetCreate,
        @Header("Authorization") token: String,
    ): Call<BudgetRead>

    @DELETE("budgets/{budgetId}")
    fun deleteBudget(
        @Path("budgetId") budgetId: Int,
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("budget/categories/")
    fun getBudgetCategories(): Call<List<BudgetCategory>>

    // Goal endpoints
    @GET("goals/")
    fun getGoals(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String,
    ): Call<List<GoalsRead>>

    @POST("goals/")
    fun createGoal(
        @Body newGoal: GoalsCreate,
        @Header("Authorization") token: String,
    ): Call<GoalsRead>

    @GET("goal/{goal_id}")
    fun getGoal(@Path("goal_id") goalId: Int): Call<GoalsRead>

    @PUT("goals/{goalId}")
    fun updateGoal(
        @Path("goalId") goalId: Int,
        @Body goal: GoalsCreate,
        @Header("Authorization") token: String,
    ): Call<GoalsRead>

    @DELETE("goals/{goalId}")
    fun deleteGoal(
        @Path("goalId") goalId: Int,
        @Header("Authorization") token: String
    ): Call<Void>

    // Report endpoints
    @POST("reports/")
    fun createReport(
        @Body report: ReportCreate,
        @Header("Authorization") token: String,
    ): Call<ReportRead>

    @GET("reports/")
    fun getReports(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String,
    ): Call<List<ReportRead>>

    @GET("report/{report_id}")
    fun getReport(
        @Path("report_id") reportId: Int,
        @Header("Authorization") token: String,
        ): Call<ReportRead>

    @PUT("reports/{reportId}")
    fun updateReport(
        @Path("reportId") reportId: Int,
        @Body report: ReportCreate,
        @Header("Authorization") token: String,
        ): Call<ReportRead>

    @DELETE("reports/{reportId}")
    fun deleteReport(
        @Path("reportId") reportId: Int,
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("report/types/")
    fun getReportTypes(): Call<List<ReportType>>

    // Transaction endpoints
    @POST("transactions/")
    fun createTransaction(
        @Body newTransaction: TransactionCreate,
        @Header("Authorization") token: String,
    ): Call<TransactionRead>

    @GET("transactions/")
    fun getTransactions(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String,
    ): Call<List<TransactionRead>>

    @GET("transaction/{transaction_id}")
    fun getTransaction(
        @Path("transaction_id") transactionId: Int,
        @Header("Authorization") token: String,
    ): Call<TransactionRead>

    @PUT("transactions/{transactionId}")
    fun updateTransaction(
        @Path("transactionId") transactionId: Int,
        @Body transaction: TransactionCreate,
        @Header("Authorization") token: String,
    ): Call<TransactionRead>

    @DELETE("transactions/{transactionId}")
    fun deleteTransaction(
        @Path("transactionId") transactionId: Int,
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("transaction/categories/")
    fun getTransactionCategories(): Call<List<TransactionCategory>>

    // Notifications endpoints
    @POST("notifications/")
    fun createNotification(@Body notification: NotificationCreate): Call<NotificationRead>

    @GET("notifications/")
    fun getNotifications(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String,
    ): Call<List<NotificationRead>>

    @GET("notification/{notification_id}")
    fun getNotification(@Path("notification_id") notificationId: Int): Call<NotificationRead>

    @PUT("notifications/{notificationId}")
    fun updateNotification(
        @Path("notificationId") notificationId: Int,
        @Body notification: NotificationCreate,
        @Header("Authorization") token: String,
    ) : Call<NotificationRead>

    @DELETE("notifications/{notificationId}")
    fun deleteNotification(
        @Path("notificationId") notificationId: Int,
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("notification/types/")
    fun getNotificationTypes(): Call<List<NotificationType>>

    // Username recovery endpoint
    @POST("username/recover/")
    fun recoverUsername(@Body recoveryRequest: UsernameRecoveryRequest): Call<Void>
}