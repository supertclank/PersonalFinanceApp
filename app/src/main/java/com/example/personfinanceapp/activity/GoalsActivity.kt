package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.RetrofitClient
import api.data_class.GoalsRead
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
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
    private lateinit var goalsAdapter: GoalAdapter
    private val goalsList = mutableListOf<GoalsRead>()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("YOUR_PREF_NAME", MODE_PRIVATE)
        token = sharedPreferences.getString("user_token", "") ?: ""

        // Set up the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Set up navigation view item selection listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                }
                R.id.nav_budgets -> {
                    startActivity(Intent(this, BudgetsActivity::class.java))
                }
                R.id.nav_goals -> {
                    startActivity(Intent(this, GoalsActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Set up RecyclerView
        goalsRecyclerView = findViewById(R.id.goals_recycler_view)
        goalsRecyclerView.layoutManager = LinearLayoutManager(this)
        goalsAdapter = GoalAdapter(goalsList)
        goalsRecyclerView.adapter = goalsAdapter

        // Fetch existing goals from the API
        fetchGoals()

        // Handle "Add Goal" button click
        findViewById<Button>(R.id.add_goal_button).setOnClickListener {
            showAddGoalDialog()
        }
    }

    override fun onBackPressed() {
        // Close the navigation drawer if it's open, otherwise handle the back press as usual
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchGoals() {
        val apiService = RetrofitClient.instance
        val call = apiService.getGoals(0, 10)

        call.enqueue(object : Callback<List<GoalsRead>> {
            override fun onResponse(call: Call<List<GoalsRead>>, response: Response<List<GoalsRead>>) {
                if (response.isSuccessful) {
                    val goals = response.body() ?: emptyList()
                    goalsList.clear()
                    goalsList.addAll(goals)
                    goalsAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to load goals", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<GoalsRead>>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddGoalDialog() {
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
                val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                deadlineInput.setText(formattedDate)
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
                } catch (e: Exception) {
                    Toast.makeText(this@GoalsActivity, "Invalid date format. Use dd-MM-yyyy.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton // Stop the process if date parsing fails
                }

                val description = descriptionInput.text.toString()
                addNewGoal(goalName, targetAmount, currentAmount, deadline, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewGoal(
        goalName: String,
        targetAmount: Double,
        currentAmount: Double,
        deadline: LocalDate, // Keep it LocalDate
        description: String
    ) {
        val userId = getUserIdFromToken(token) // Retrieve the user ID from the token
        val deadlineString = deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // Format as "yyyy-MM-dd"

        val newGoal = api.data_class.GoalsCreate(
            name = goalName,
            target_amount = targetAmount,
            current_amount = currentAmount,
            deadline = deadlineString,
            description = description,
            user_id = userId
        )

        val apiService = RetrofitClient.instance
        val call = apiService.createGoal(newGoal)

        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    fetchGoals() // Refresh the goals list
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to create goal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getUserIdFromToken(token: String): Int {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("user_id").asInt() ?: 0 // Adjust this according to your token structure
        } catch (e: Exception) {
            0 // Return a default value or handle error as needed
        }
    }
}