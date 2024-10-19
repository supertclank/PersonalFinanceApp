package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import com.example.personfinanceapp.R
import com.google.android.material.navigation.NavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class GoalsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Initialize the DrawerLayout and NavigationView
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

                }

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
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

    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var goalsAdapter: GoalAdapter
    private val goalsList = mutableListOf<GoalsRead>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals)

        // Set up RecyclerView
        goalsRecyclerView = findViewById(R.id.goals_recycler_view)
        goalsRecyclerView.layoutManager = LinearLayoutManager(this)
        goalsAdapter = GoalAdapter(goalsList)
        goalsRecyclerView.adapter = goalsAdapter

        // Fetch existing goals from the API
        fetchGoals()

        // Handle "Add Goal" button click
        findViewById<Button>(R.id.add_goal_button).setOnClickListener {
            addNewGoal()
        }
    }

    private fun fetchGoals() {
        val apiService = RetrofitClient.instance
        val call = apiService.getGoals(0, 10)

        call.enqueue(object : Callback<List<GoalsRead>> {
            override fun onResponse(
                call: Call<List<GoalsRead>>,
                response: Response<List<GoalsRead>>
            ) {
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_name_input)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.goal_target_amount_input)
        val currentAmountInput = dialogView.findViewById<EditText>(R.id.goal_current_amount_input)
        val deadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline_input)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.goal_description_input)

        AlertDialog.Builder(this)
            .setTitle("Add New Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val goalName = goalNameInput.text.toString()
                val targetAmount = targetAmountInput.text.toString().toDoubleOrNull() ?: 0.0 // Handle input
                val currentAmount = currentAmountInput.text.toString().toDoubleOrNull() ?: 0.0 // Handle input
                val deadline = deadlineInput.text.toString() // You can format this as needed
                val description = descriptionInput.text.toString()

                addNewGoal(goalName, targetAmount, currentAmount, deadline, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewGoal(goalName: String, targetAmount: Double, currentAmount: Double, deadline: String, description: String) {
        val newGoal = GoalsCreate(goalName, targetAmount, currentAmount, deadline, description)

        val apiService = RetrofitClient.instance
        val call = apiService.createGoal(newGoal)

        call.enqueue(object : Callback<GoalsRead> {
            override fun onResponse(call: Call<GoalsRead>, response: Response<GoalsRead>) {
                if (response.isSuccessful) {
                    // Refresh the goals list
                    fetchGoals()
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to create goal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoalsRead>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}