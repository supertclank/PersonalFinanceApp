package api

import api.data_class.BudgetCreate
import api.data_class.BudgetRead
import api.data_class.GoalsCreate
import api.data_class.GoalsRead
import api.data_class.NotificationCreate
import api.data_class.NotificationRead
import api.data_class.ProfileCreate
import api.data_class.ProfileRead
import api.data_class.ReportCreate
import api.data_class.ReportRead
import api.data_class.TransactionCreate
import api.data_class.TransactionRead
import api.data_class.UserCreate
import api.data_class.UserRead
import api.data_class.LoginRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // User endpoints
    @POST("user/")
    fun createUser(@Body user: UserCreate): Call<UserRead>

    @GET("user/")
    fun getUsers(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<UserRead>>

    @GET("/user/{user_id}")
    fun getUser(@Path("user_id") userId: Int): Call<UserRead>

    @POST("user/login/") // Adjust the endpoint URL based on your backend
    fun login(@Body request: LoginRequest): Call<UserRead>

    // Profile endpoints
    @POST("profile/")
    fun createProfile(@Body profile: ProfileCreate): Call<ProfileRead>

    //@GET("profile/")
    //fun getProfiles(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<ProfileRead>>

    @GET("profile/{profile_id}")
    fun getProfile(@Path("profile_id") profileId: Int): Call<ProfileRead>

    // Budget endpoints
    @POST("budget/")
    fun createBudget(@Body budget: BudgetCreate): Call<BudgetRead>

    @GET("budget/")
    fun getBudgets(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<BudgetRead>>

    @GET("budget/{budget_id}")
    fun getBudget(@Path("budget_id") budgetId: Int): Call<BudgetRead>

    // Goal endpoints
    @POST("goal/")
    fun createGoal(@Body goal: GoalsCreate): Call<GoalsRead>

    @GET("goal/")
    fun getGoals(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<GoalsRead>>

    @GET("goal/{goal_id}")
    fun getGoal(@Path("goal_id") goalId: Int): Call<GoalsRead>

    // Report endpoints
    @POST("report/")
    fun createReport(@Body report: ReportCreate): Call<ReportRead>

    @GET("report/")
    fun getReports(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<ReportRead>>

    @GET("report/{report_id}")
    fun getReport(@Path("report_id") reportId: Int): Call<ReportRead>

    // Transaction endpoints
    @POST("transaction/")
    fun createTransaction(@Body transaction: TransactionCreate): Call<TransactionRead>

    @GET("transaction/")
    fun getTransactions(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<TransactionRead>>

    @GET("transaction/{transaction_id}")
    fun getTransaction(@Path("transaction_id") transactionId: Int): Call<TransactionRead>

    // Notifications endpoints
    @POST("notification/")
    fun createNotification(@Body notification: NotificationCreate): Call<NotificationRead>

    @GET("notification/")
    fun getNotifications(@Query("skip") skip: Int, @Query("limit") limit: Int): Call<List<NotificationRead>>

    @GET("notification/{notification_id}")
    fun getNotification(@Path("notification_id") notificationId: Int): Call<NotificationRead>
}
