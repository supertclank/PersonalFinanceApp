package api

import api.data_class.BudgetCreate
import api.data_class.BudgetRead
import api.data_class.GoalsCreate
import api.data_class.GoalsRead
import api.data_class.NotificationCreate
import api.data_class.NotificationRead
import api.data_class.ReportCreate
import api.data_class.ReportRead
import api.data_class.TokenResponse
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
    fun createBudget(@Body budget: BudgetCreate): Call<BudgetRead>

    @GET("budgets/")
    fun getBudgets(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<BudgetRead>>

    @GET("budget/{budget_id}")
    fun getBudget(@Path("budget_id") budgetId: Int): Call<BudgetRead>

    @PUT("budgets/{budgetId}")
    fun updateBudget(
        @Path("budgetId") budgetId: Int,
        @Body budget: BudgetCreate,
    ): Call<BudgetRead>

    @DELETE("budgets/{budgetId}")
    fun deleteBudget(@Path("budgetId") budgetId: Int): Call<Void>

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
    fun deleteGoal(@Path("goalId") goalId: Int, @Header("Authorization") token: String): Call<Void>

    // Report endpoints
    @POST("reports/")
    fun createReport(@Body report: ReportCreate): Call<ReportRead>

    @GET("reports/")
    fun getReports(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<ReportRead>>

    @GET("report/{report_id}")
    fun getReport(@Path("report_id") reportId: Int): Call<ReportRead>

    // Transaction endpoints
    @POST("transactions/")
    fun createTransaction(@Body transaction: TransactionCreate): Call<TransactionRead>

    @GET("transactions/")
    fun getTransactions(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
    ): Call<List<TransactionRead>>

    @GET("transaction/{transaction_id}")
    fun getTransaction(@Path("transaction_id") transactionId: Int): Call<TransactionRead>

    // Notifications endpoints
    @POST("notifications/")
    fun createNotification(@Body notification: NotificationCreate): Call<NotificationRead>

    @GET("notifications/")
    fun getNotifications(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
    ): Call<List<NotificationRead>>

    @GET("notification/{notification_id}")
    fun getNotification(@Path("notification_id") notificationId: Int): Call<NotificationRead>

    // Username recovery endpoint
    @POST("username/recover/")
    fun recoverUsername(@Body recoveryRequest: UsernameRecoveryRequest): Call<Void>
}