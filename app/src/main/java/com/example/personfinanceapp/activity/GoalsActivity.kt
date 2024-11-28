package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import api.RetrofitClient
import api.data_class.GoalsCreate
import api.data_class.GoalsRead
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoalsActivity : BaseActivity() {

    private val goalsList = mutableListOf<GoalsRead>()
    private val TAG = "GoalsActivity"
    private lateinit var token: String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: Initializing the activity")

        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(R.layout.goals, contentFrame, true)

        // Retrieve token using TokenUtil
        token = TokenUtils.getTokenFromStorage(this) ?: run {
            Log.e(TAG, "onCreate: Token is null")
            Toast.makeText(this, "Error: No valid token", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "onCreate: Token retrieved: $token")

        // Initialize SwipeRefreshLayout
        setupSwipeRefreshLayout()
        Log.d(TAG, "onCreate: SwipeRefreshLayout initialized")

        // Fetch existing goals from the API
        Log.d(TAG, "onCreate: Fetching existing goals from the API")
        fetchGoals(token)

        // Handle "Add Goal" button click
        findViewById<Button>(R.id.add_goal_button).setOnClickListener {
            Log.d(TAG, "onCreate: Add Goal button clicked")
            showAddGoalDialog(token)
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout) // Initialize here

        swipeRefreshLayout.setOnRefreshListener {
            fetchGoals(token)
        }
    }

    private fun fetchGoals(token: String) {
        Log.d(TAG, "fetchGoals: Fetching goals from API")

        // Extract user ID from the token
        val userId = getUserIdFromToken(token)
        if (userId == -1) {
            Log.e(TAG, "fetchGoals: Invalid user ID. Cannot fetch goals.")
            Toast.makeText(this, "Unable to retrieve user data.", Toast.LENGTH_SHORT).show()
            // Optionally stop refreshing here if you have a SwipeRefreshLayout
            return
        }

        val apiService = RetrofitClient.instance
        Log.d(TAG, "fetchGoals: Formatted auth token")

        // Start the API call
        apiService.getGoals(0, 10, "Bearer $token").enqueue(object : Callback<List<GoalsRead>> {
            override fun onResponse(
                call: Call<List<GoalsRead>>,
                response: Response<List<GoalsRead>>,
            ) {
                Log.d(TAG, "fetchGoals: Received API response with code: ${response.code()}")

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val goals = response.body() ?: run {
                        Log.e(TAG, "fetchGoals: Response body is null")
                        Toast.makeText(this@GoalsActivity, "No goals found.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    Log.d(TAG, "fetchGoals: Goals loaded successfully: $goals")

                    // Clear the existing goals list and add new ones
                    goalsList.clear()
                    goalsList.addAll(goals)

                    // Update the UI with the new goals
                    displayGoals(goalsList)

                    Log.d(TAG, "fetchGoals: Displayed goals; total count: ${goalsList.size}")
                } else {
                    Log.e(TAG, "fetchGoals: Error ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        this@GoalsActivity,
                        "Failed to retrieve goals.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<GoalsRead>>, t: Throwable) {
                Log.e(TAG, "fetchGoals: API call failed: ${t.message}", t)
                Toast.makeText(
                    this@GoalsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    fun displayGoals(goals: List<GoalsRead>) {
        val goalsContainer: LinearLayout = findViewById(R.id.goalsContainer)
        goalsContainer.removeAllViews() // Clear any existing views

        for (goal in goals) {
            // Inflate the goal item layout
            val goalView =
                LayoutInflater.from(this).inflate(R.layout.goal_item, goalsContainer, false)

            // Set the values for the goal item
            goalView.findViewById<TextView>(R.id.goal_title).text = goal.name
            goalView.findViewById<TextView>(R.id.goal_description).text =
                goal.description ?: "No description"
            goalView.findViewById<TextView>(R.id.goal_deadline).text = goal.deadline

            // Set the current and target amounts
            goalView.findViewById<TextView>(R.id.goal_current_amount).text =
                "£${goal.current_amount}"
            goalView.findViewById<TextView>(R.id.goal_target_amount).text =
                "£${goal.target_amount}"

            val deleteButton = goalView.findViewById<Button>(R.id.button_delete)
            deleteButton.setOnClickListener {
                deleteGoal(goal.id)
            }

            val editButton = goalView.findViewById<Button>(R.id.button_edit)
            editButton.setOnClickListener {
                editGoal(goal)
            }

            // Add the goal view to the container
            goalsContainer.addView(goalView)
        }
    }

    private fun editGoal(goal: GoalsRead) {
        // Use the updated dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)

        val goalNameInput = dialogView.findViewById<EditText>(R.id.edit_goal_name)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.edit_goal_target_amount)
        val currentAmountInput = dialogView.findViewById<EditText>(R.id.edit_goal_current_amount)
        val deadlineInput = dialogView.findViewById<EditText>(R.id.edit_goal_deadline)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.edit_goal_description)

        // Prepopulate the fields with current goal data
        goalNameInput.setText(goal.name)
        targetAmountInput.setText(goal.target_amount.toString())
        currentAmountInput.setText(goal.current_amount.toString())
        deadlineInput.setText(goal.deadline)
        descriptionInput.setText(goal.description)

        AlertDialog.Builder(this)
            .setTitle("Edit Goal")
            .setView(dialogView)
            .setPositiveButton(R.string.submit) { _, _ ->
                // Get user ID from token
                val userId = getUserIdFromToken(token)
                if (userId == -1) {
                    Toast.makeText(
                        this,
                        "Error: Invalid user ID. Cannot update goal.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Create updated goal object with user input
                val updatedGoal = GoalsCreate(
                    user_id = userId,
                    name = goalNameInput.text.toString(),
                    target_amount = targetAmountInput.text.toString().toDouble(),
                    current_amount = currentAmountInput.text.toString().toDouble(),
                    deadline = deadlineInput.text.toString(),
                    description = descriptionInput.text.toString()
                )
                // Call updateGoal function to save changes
                updateGoal(goal.id, updatedGoal, token)
            }
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun deleteGoal(goalId: Int) {
        Log.d(TAG, "deleteGoal: Deleting goal with ID: $goalId")

        val apiService = RetrofitClient.instance
        val call = apiService.deleteGoal(goalId, "Bearer $token")

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "deleteGoal: Successfully deleted goal with ID: $goalId")
                    Toast.makeText(this@GoalsActivity, "Goal deleted", Toast.LENGTH_SHORT).show()
                    fetchGoals(token) // Refresh the goals list
                } else {
                    Log.e(TAG, "deleteGoal: Failed to delete goal. Error code: ${response.code()}")
                    Toast.makeText(this@GoalsActivity, "Failed to delete goal", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "deleteGoal: Error deleting goal: ${t.message}", t)
                Toast.makeText(this@GoalsActivity, "Error deleting goal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateGoal(goalId: Int, updatedGoal: GoalsCreate, token: String) {
        Log.d(TAG, "updateGoal: Updating goal with ID: $goalId")

        val apiService = RetrofitClient.instance
        apiService.updateGoal(goalId, updatedGoal, "Bearer ${this.token}")
            .enqueue(object : Callback<GoalsRead> {
                override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                    Log.d(TAG, "updateGoal: Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(TAG, "updateGoal: Successfully updated goal with ID: $goalId")
                        Toast.makeText(
                            this@GoalsActivity,
                            "Goal updated successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchGoals(this@GoalsActivity.token) // Refresh the goals list
                    } else {
                        Log.e(TAG, "updateGoal: Failed to update goal: ${response.message()}")
                        Toast.makeText(
                            this@GoalsActivity,
                            "Error updating goal.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                    Log.e(TAG, "updateGoal: API call failed: ${t.message}", t)
                    Toast.makeText(
                        this@GoalsActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showAddGoalDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.add_goal_name)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.add_goal_target_amount)
        val currentAmountInput = dialogView.findViewById<EditText>(R.id.add_goal_current_amount)
        val deadlineInput = dialogView.findViewById<EditText>(R.id.add_goal_deadline)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.add_goal_description)

        // Set up the DatePickerDialog
        deadlineInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Format and set the selected date to the EditText in YYYY-MM-DD format
                val formattedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                deadlineInput.setText(formattedDate)
            }, year, month, day).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Get user ID from token
                val userId = getUserIdFromToken(token)
                if (userId == -1) {
                    Toast.makeText(
                        this,
                        "Error: Invalid user ID. Cannot create goal.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Validate input fields
                val goalName = goalNameInput.text.toString()
                val targetAmountStr = targetAmountInput.text.toString()
                val currentAmountStr = currentAmountInput.text.toString()
                val deadline = deadlineInput.text.toString()
                val description = descriptionInput.text.toString()

                // Check for empty fields
                if (goalName.isEmpty() || targetAmountStr.isEmpty() || currentAmountStr.isEmpty() || deadline.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Validate the deadline format
                val datePattern = Regex("\\d{4}-\\d{2}-\\d{2}") // YYYY-MM-DD pattern
                if (!deadline.matches(datePattern)) {
                    Toast.makeText(
                        this,
                        "Please enter a valid date in the format YYYY-MM-DD",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Convert amounts to Double
                val targetAmount = targetAmountStr.toDoubleOrNull()
                val currentAmount = currentAmountStr.toDoubleOrNull()

                // Validate amounts
                if (targetAmount == null || currentAmount == null) {
                    Toast.makeText(this, "Please enter valid amounts", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Check that the current amount is not higher than the target amount
                if (currentAmount > targetAmount) {
                    Toast.makeText(
                        this,
                        "Current amount cannot be higher than target amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val newGoal = GoalsCreate(
                    user_id = userId, // Pass user ID here
                    name = goalName,
                    target_amount = targetAmount,
                    current_amount = currentAmount,
                    deadline = deadline,
                    description = description
                )
                createGoal(newGoal, token) // Create goal and refresh list
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createGoal(newGoal: GoalsCreate, token: String) {
        val apiService = RetrofitClient.instance
        val call = apiService.createGoal(newGoal, "Bearer $token")

        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "createGoal: Successfully created goal: ${response.body()}")
                    Toast.makeText(this@GoalsActivity, "Goal created", Toast.LENGTH_SHORT).show()
                    fetchGoals(token) // Refresh the goals list
                } else {
                    Log.e(TAG, "createGoal: Failed to create goal. Error code: ${response.code()}")
                    Toast.makeText(this@GoalsActivity, "Failed to create goal", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Log.e(TAG, "createGoal: Error creating goal: ${t.message}", t)
                Toast.makeText(this@GoalsActivity, "Error creating goal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdFromToken(token: String): Int {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("id").asInt() ?: -1 // Return -1 if user ID not found
        } catch (e: Exception) {
            Log.e(TAG, "getUserIdFromToken: Error decoding token: ${e.message}")
            -1
        }
    }
}