package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.RetrofitClient
import api.data_class.GoalsCreate
import api.data_class.GoalsRead
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.RecyclerView.GoalsAdapter
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


class GoalsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var goalsAdapter: GoalsAdapter
    private val goalsList = mutableListOf<GoalsRead>()
    private val TAG = "GoalsActivity"
    private lateinit var token: String // Declare the token as a property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals)

        Log.d(TAG, "onCreate: Initializing the activity")

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Retrieve token using TokenUtil
        token = TokenUtils.getTokenFromStorage(this) ?: run {
            Log.e(TAG, "onCreate: Token is null")
            Toast.makeText(this, "Error: No valid token", Toast.LENGTH_SHORT).show()
            return // Exit if no token is available
        }

        Log.d(TAG, "onCreate: Token retrieved: $token")

        // Set up the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Navigation item selection listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_transactions -> startActivity(
                    Intent(
                        this,
                        TransactionsActivity::class.java
                    )
                )

                R.id.nav_reports -> startActivity(Intent(this, ReportsActivity::class.java))
                R.id.nav_budgets -> startActivity(Intent(this, BudgetsActivity::class.java))
                R.id.nav_goals -> startActivity(Intent(this, GoalsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Set up RecyclerView for goals
        goalsRecyclerView = findViewById(R.id.goals_recycler_view)
        goalsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        goalsAdapter = GoalsAdapter(object : GoalsAdapter.GoalsListener {
            override fun onGoalClick(goal: GoalsRead) {
                showGoalDetailsDialog(goal)
            }

            override fun onEditGoal(goal: GoalsRead) {
                editGoal(goal) // Call your editGoal function here
            }

            override fun onDeleteGoal(goal: GoalsRead) {
                deleteGoal(goal.id) // Call your deleteGoal function here
            }
        })
        goalsRecyclerView.adapter = goalsAdapter

        // Fetch existing goals from the API
        fetchGoals(token)

        // Handle "Add Goal" button click
        findViewById<Button>(R.id.add_goal_button).setOnClickListener {
            showAddGoalDialog(token) // Pass the token here
        }
    }

    override fun onBackPressed() {
        // Close the navigation drawer if it's open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchGoals(token: String) {
        Log.d(TAG, "fetchGoals: Fetching goals from API")

        // Extract user ID from the token
        val userId = getUserIdFromToken(token)
        if (userId == -1) {
            Log.e(TAG, "fetchGoals: Invalid user ID. Cannot fetch goals.")
            Toast.makeText(this, "Error: Invalid user ID. Cannot fetch goals.", Toast.LENGTH_SHORT).show()
            return  // Exit the function if the user ID is invalid
        }

        val apiService = RetrofitClient.instance
        val authToken = "Bearer $token"  // Format the token properly
        Log.d(TAG, "fetchGoals: Auth token formatted: $authToken")

        val call = apiService.getGoals(0, 10, authToken) // Pass the token in the header

        call.enqueue(object : Callback<List<GoalsRead>> {
            override fun onResponse(
                call: Call<List<GoalsRead>>,
                response: Response<List<GoalsRead>>,
            ) {
                Log.d(TAG, "fetchGoals: API response received, code: ${response.code()}")

                if (response.isSuccessful) {
                    val goals = response.body() ?: emptyList()
                    Log.d(TAG, "fetchGoals: Goals loaded from response: $goals")
                    goalsList.clear()
                    goalsList.addAll(goals)
                    goalsAdapter.notifyDataSetChanged()
                    Log.d(TAG, "fetchGoals: Goals list updated, total goals: ${goalsList.size}")
                } else {
                    Log.e(TAG, "fetchGoals: Error ${response.code()}: ${response.message()}")
                    Log.e(TAG, "fetchGoals: Response body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@GoalsActivity, "Failed to load goals", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<GoalsRead>>, t: Throwable) {
                Log.e(TAG, "fetchGoals: API call failed: ${t.message}", t)
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showGoalDetailsDialog(goal: GoalsRead) {
        Log.d(TAG, "showGoalDetailsDialog: Displaying details for goal: ${goal.name}")

        val dialogView = layoutInflater.inflate(R.layout.dialog_goal_details, null)
        val goalNameTextView = dialogView.findViewById<TextView>(R.id.goal_name_text_view)
        val targetAmountTextView =
            dialogView.findViewById<TextView>(R.id.goal_target_amount_text_view)
        val currentAmountTextView =
            dialogView.findViewById<TextView>(R.id.goal_current_amount_text_view)
        val deadlineTextView = dialogView.findViewById<TextView>(R.id.goal_deadline_text_view)
        val descriptionTextView = dialogView.findViewById<TextView>(R.id.goal_description_text_view)

        goalNameTextView.text = goal.name
        targetAmountTextView.text = goal.target_amount.toString()
        currentAmountTextView.text = goal.current_amount.toString()
        deadlineTextView.text = goal.deadline.toString() // Format as needed
        descriptionTextView.text = goal.description

        AlertDialog.Builder(this)
            .setTitle("Goal Details")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                // Call your edit function here
                editGoal(goal)
            }
            .setNegativeButton("Delete") { _, _ ->
                // Call your delete function here
                deleteGoal(goal.id)
            }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun editGoal(goal: GoalsRead) {
        // Show a dialog to edit the goal
        val dialogView = layoutInflater.inflate(R.layout.goal_item, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_title)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.goal_target_amount)
        val currentAmountInput = dialogView.findViewById<EditText>(R.id.goal_current_amount)
        val deadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.goal_description)

        // Populate the fields with existing goal data
        goalNameInput.setText(goal.name)
        targetAmountInput.setText(goal.target_amount.toString())
        currentAmountInput.setText(goal.current_amount.toString())
        deadlineInput.setText(goal.deadline)
        descriptionInput.setText(goal.description)

        AlertDialog.Builder(this)
            .setTitle("Edit Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedGoal = GoalsCreate(
                    name = goalNameInput.text.toString(),
                    target_amount = targetAmountInput.text.toString().toDouble(),
                    current_amount = currentAmountInput.text.toString().toDouble(),
                    deadline = deadlineInput.text.toString(),
                    description = descriptionInput.text.toString(),
                    user_id = goal.user_id
                )
                updateGoal(goal.id, updatedGoal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateGoal(goalId: Int, goal: GoalsCreate) {
        val apiService = RetrofitClient.instance
        val authToken = "Bearer $token"
        val call = apiService.updateGoal(goalId, goal, authToken)

        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@GoalsActivity,
                        "Goal updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchGoals(token) // Refresh the list
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to update goal", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteGoal(goalId: Int) {
        val apiService = RetrofitClient.instance
        val authToken = "Bearer $token"
        val call = apiService.deleteGoal(goalId, authToken)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@GoalsActivity,
                        "Goal deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchGoals(token) // Refresh the list
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to delete goal", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddGoalDialog(token: String) {
        Log.d(TAG, "showAddGoalDialog: Displaying add goal dialog")
        val dialogView = layoutInflater.inflate(R.layout.goal_item, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_title)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.goal_target_amount)
        val currentAmountInput = dialogView.findViewById<EditText>(R.id.goal_current_amount)
        val deadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.goal_description)

        // Initialize a Calendar instance
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Set a click listener for the deadline input
        deadlineInput.setOnClickListener {
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date to dd-MM-yyyy
                val formattedDate =
                    String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                deadlineInput.setText(formattedDate)
                Log.d(TAG, "showAddGoalDialog: Selected deadline date: $formattedDate")
            }, year, month, day).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val goalName = goalNameInput.text.toString()
                val targetAmount = targetAmountInput.text.toString().toDoubleOrNull() ?: 0.0
                val currentAmount = currentAmountInput.text.toString().toDoubleOrNull() ?: 0.0

                // Parse the deadline input into a LocalDate
                val deadlineString = deadlineInput.text.toString()
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val deadline: LocalDate?

                try {
                    deadline = LocalDate.parse(deadlineString, formatter)
                    Log.d(TAG, "showAddGoalDialog: Parsed deadline: $deadline")
                } catch (e: Exception) {
                    Toast.makeText(
                        this@GoalsActivity,
                        "Invalid date format. Use dd-MM-yyyy.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "showAddGoalDialog: Date parsing failed: ${e.message}")
                    return@setPositiveButton // Stop the process if date parsing fails
                }

                val description = descriptionInput.text.toString()
                Log.d(
                    TAG,
                    "showAddGoalDialog: Creating goal with name: $goalName, target amount: $targetAmount, current amount: $currentAmount, deadline: $deadline, description: $description"
                )
                addNewGoal(goalName, targetAmount, currentAmount, deadline, description, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewGoal(
        goalName: String,
        targetAmount: Double,
        currentAmount: Double,
        deadline: LocalDate,
        description: String,
        token: String,
    ) {
        Log.d(TAG, "addNewGoal: Adding new goal with name: $goalName")
        val userId = getUserIdFromToken(token)
        Log.d(TAG, "addNewGoal: User ID retrieved from token: $userId")

        val deadlineString = deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        Log.d(TAG, "addNewGoal: Deadline formatted for API: $deadlineString")

        val newGoal = GoalsCreate(
            name = goalName,
            target_amount = targetAmount,
            current_amount = currentAmount,
            deadline = deadlineString,
            description = description,
            user_id = userId
        )

        val apiService = RetrofitClient.instance
        val authToken = "Bearer $token"  // Format the token properly
        Log.d(TAG, "addNewGoal: Making API call to create goal with token: $authToken")

        val call = apiService.createGoal(newGoal, authToken) // Pass the token in the header

        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "addNewGoal: Goal created successfully: ${response.body()}")
                    Toast.makeText(
                        this@GoalsActivity,
                        "Goal added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchGoals(token) // Refresh goals after adding a new one
                } else {
                    Log.e(TAG, "addNewGoal: Error ${response.code()}: ${response.message()}")
                    Toast.makeText(this@GoalsActivity, "Failed to add goal", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Log.e(TAG, "addNewGoal: API call failed: ${t.message}", t)
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdFromToken(token: String): Int {
        val jwt = JWT(token)

        // Log all claims for debugging
        val claims = jwt.claims
        claims.forEach { (key, value) ->
            Log.d(TAG, "Claim: $key = ${value.asString()}")
        }

        // Extract the user_id from the JWT claims
        val userIdClaim = jwt.getClaim("id").asInt() // Use "id" instead of "user_id"
        if (userIdClaim == null) {
            Log.e(TAG, "getUserIdFromToken: user_id claim not found in the token")
            Toast.makeText(this, "Error: User ID not found in token", Toast.LENGTH_SHORT).show()
            return -1 // Return an invalid ID or handle error case appropriately
        }
        return userIdClaim
    }

}