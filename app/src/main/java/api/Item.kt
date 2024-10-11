import java.math.BigDecimal
import java.util.Date

// User data classes
data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val password: String,
    val createdAt: Date
)

data class UserCreate(
    val username: String,
    val email: String,
    val hashed_password: String
)

data class UserRead(
    val userId: Int,
    val username: String,
    val email: String,
    val password: String,
    val createdAt: Date
)

// Profile data classes
data class Profile(
    val profileId: Int,
    val userId: Int,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String?,
    val createdAt: Date
)

data class ProfileCreate(
    val userId: Int,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String?
)

data class ProfileRead(
    val profileId: Int,
    val userId: Int,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String?,
    val createdAt: Date
)

// Budget data classes
data class Budget(
    val budgetId: Int,
    val userId: Int,
    val budgetCategoryId: Int,
    val amount: BigDecimal,
    val startDate: Date?,
    val endDate: Date?
)

data class BudgetCreate(
    val userId: Int,
    val budgetCategoryId: Int,
    val amount: BigDecimal,
    val startDate: Date?,
    val endDate: Date?
)

data class BudgetRead(
    val budgetId: Int,
    val userId: Int,
    val budgetCategoryId: Int,
    val amount: BigDecimal,
    val startDate: Date?,
    val endDate: Date?
)

// Goal data classes
data class Goal(
    val goalId: Int,
    val userId: Int,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val deadline: Date?,
    val description: String?
)

data class GoalsCreate(
    val userId: Int,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val deadline: Date?,
    val description: String?
)

data class GoalsRead(
    val goalId: Int,
    val userId: Int,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val deadline: Date?,
    val description: String?
)

// Report data classes
data class Report(
    val reportId: Int,
    val userId: Int,
    val reportTypeId: Int,
    val generatedAt: Date,
    val data: Map<String, Any>? // JSON is represented as a map in Kotlin
)

data class ReportCreate(
    val userId: Int,
    val reportTypeId: Int,
    val generatedAt: Date,
    val data: Map<String, Any>? // JSON is represented as a map in Kotlin
)

data class ReportRead(
    val reportId: Int,
    val userId: Int,
    val reportTypeId: Int,
    val generatedAt: Date,
    val data: Map<String, Any>? // JSON is represented as a map in Kotlin
)

// Transaction data classes
data class Transaction(
    val transactionId: Int,
    val userId: Int,
    val amount: BigDecimal,
    val transactionCategoryId: Int,
    val date: Date,
    val description: String?
)

data class TransactionCreate(
    val userId: Int,
    val amount: BigDecimal,
    val transactionCategoryId: Int,
    val date: Date,
    val description: String?
)

data class TransactionRead(
    val transactionId: Int,
    val userId: Int,
    val amount: BigDecimal,
    val transactionCategoryId: Int,
    val date: Date,
    val description: String?
)

// Notification data classes
data class Notification(
    val notificationId: Int,
    val userId: Int,
    val message: String,
    val notificationTypeId: Int,
    val isRead: Boolean,
    val createdAt: Date
)

data class NotificationCreate(
    val userId: Int,
    val message: String,
    val notificationTypeId: Int,
    val isRead: Boolean
)

data class NotificationRead(
    val notificationId: Int,
    val userId: Int,
    val message: String,
    val notificationTypeId: Int,
    val isRead: Boolean,
    val createdAt: Date
)
