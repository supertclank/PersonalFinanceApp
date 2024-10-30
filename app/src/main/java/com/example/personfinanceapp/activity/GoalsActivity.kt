package com.example.personfinanceapp.activity

import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
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
                R.id.nav_transactions -> startActivity(Intent(this, TransactionsActivity::class.java))
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
        goalsRecyclerView.layoutManager = LinearLayoutManager(this)
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

        // Make the API call to fetch goals
        val call = apiService.getGoals(0, 10, authToken)

        call.enqueue(object : Callback<List<GoalsRead>> {
            override fun onResponse(call: Call<List<GoalsRead>>, response: Response<List<GoalsRead>>) {
                Log.d(TAG, "fetchGoals: API response received, code: ${response.code()}")

                if (response.isSuccessful) {
                    val goals = response.body()
                    if (goals != null) {
                        Log.d(TAG, "fetchGoals: Goals loaded from response: $goals")

                        // Update goals list and notify the adapter
                        goalsList.clear()  // Clear the existing goals
                        goalsList.addAll(goals)  // Add new goals
                        goalsAdapter.notifyDataSetChanged()  // Notify adapter of data change

                        Log.d(TAG, "fetchGoals: Goals list updated, total goals: ${goalsList.size}")
                    } else {
                        Log.e(TAG, "fetchGoals: Response body is null")
                        Toast.makeText(this@GoalsActivity, "No goals found", Toast.LENGTH_SHORT).show()
                    }
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
        val targetAmountTextView = dialogView.findViewById<TextView>(R.id.goal_target_amount_text_view)
        val currentAmountTextView = dialogView.findViewById<TextView>(R.id.goal_current_amount_text_view)
        val deadlineTextView = dialogView.findViewById<TextView>(R.id.goal_deadline_text_view)
        val descriptionTextView = dialogView.findViewById<TextView>(R.id.goal_description_text_view)

        goalNameTextView.text = goal.name
        targetAmountTextView.text = goal.target_amount.toString()
        currentAmountTextView.text = goal.current_amount.toString()
        deadlineTextView.text = goal.deadline
        descriptionTextView.text = goal.description

        AlertDialog.Builder(this)
            .setTitle("Goal Details")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                editGoal(goal)
            }
            .setNegativeButton("Delete") { _, _ ->
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

    private fun updateGoal(goalId: Int, updatedGoal: GoalsCreate) {
        val apiService = RetrofitClient.instance
        val authToken = "Bearer $token"

        val call = apiService.updateGoal(goalId, updatedGoal, authToken)
        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "updateGoal: Goal updated successfully")
                    Toast.makeText(this@GoalsActivity, "Goal updated successfully", Toast.LENGTH_SHORT).show()
                    fetchGoals(token) // Refresh the list after updating
                } else {
                    Log.e(TAG, "updateGoal: Error ${response.code()}: ${response.message()}")
                    Toast.makeText(this@GoalsActivity, "Failed to update goal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Log.e(TAG, "updateGoal: API call failed: ${t.message}", t)
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteGoal(goalId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Delete") { _, _ ->
                val apiService = RetrofitClient.instance
                val authToken = "Bearer $token"

                val call = apiService.deleteGoal(goalId, authToken)
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "deleteGoal: Goal deleted successfully")
                            Toast.makeText(this@GoalsActivity, "Goal deleted successfully", Toast.LENGTH_SHORT).show()
                            fetchGoals(token) // Refresh the list after deletion
                        } else {
                            Log.e(TAG, "deleteGoal: Error ${response.code()}: ${response.message()}")
                            Toast.makeText(this@GoalsActivity, "Failed to delete goal", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e(TAG, "deleteGoal: API call failed: ${t.message}", t)
                        Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddGoalDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.goal_item, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_title)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.goal_target_amount)
        val currentAmountInput = dialogView.findViewById<EditText>(R.id.goal_current_amount)
        val deadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.goal_description)

        AlertDialog.Builder(this)
            .setTitle("Add Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val newGoal = GoalsCreate(
                    name = goalNameInput.text.toString(),
                    target_amount = targetAmountInput.text.toString().toDouble(),
                    current_amount = currentAmountInput.text.toString().toDouble(),
                    deadline = deadlineInput.text.toString(),
                    description = descriptionInput.text.toString(),
                    user_id = getUserIdFromToken(token) // Pass the user ID here
                )
                createGoal(newGoal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createGoal(goal: GoalsCreate) {
        val apiService = RetrofitClient.instance
        val authToken = "Bearer $token"

        val call = apiService.createGoal(goal, authToken)
        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "createGoal: Goal created successfully")
                    Toast.makeText(this@GoalsActivity, "Goal created successfully", Toast.LENGTH_SHORT).show()
                    fetchGoals(token) // Refresh the list after creation
                } else {
                    Log.e(TAG, "createGoal: Error ${response.code()}: ${response.message()}")
                    Toast.makeText(this@GoalsActivity, "Failed to create goal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Log.e(TAG, "createGoal: API call failed: ${t.message}", t)
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdFromToken(token: String): Int {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("id").asInt() ?: -1 // Return -1 if the claim is not found
        } catch (e: Exception) {
            Log.e(TAG, "getUserIdFromToken: Error parsing token: ${e.message}", e)
            -1 // Return -1 if an error occurs
        }
    }
}