package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import api.RetrofitClient
import api.data_class.GoalsRead
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
import api.data_class.BudgetRead
import api.data_class.BudgetCreate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BudgetsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val budgetsList = mutableListOf<BudgetRead>()
    private val TAG = "BudgetsActivity"
    private lateinit var token: String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budgets)

        Log.d(TAG, "onCreate: Initializing the activity")

        // Retrieve token using TokenUtil
        token = TokenUtils.getTokenFromStorage(this) ?: run {
            Log.e(TAG, "onCreate: Token is null")
            Toast.makeText(this, "Error: No valid token", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "onCreate: Token retrieved: $token")

        val toolbar: Toolbar = findViewById(R.id.toolbar) // Initialize toolbar
        setSupportActionBar(toolbar) // Set the toolbar as the action bar

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Set up toggle for the navigation drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        Log.d(TAG, "onCreate: Navigation drawer toggle set up")

        // Handle navigation item selections
        navigationView.setNavigationItemSelectedListener { menuItem ->
            Log.d(TAG, "onCreate: Navigation item selected: ${menuItem.itemId}")
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_transactions -> startActivity(Intent(this, TransactionsActivity::class.java))
                R.id.nav_reports -> startActivity(Intent(this, ReportsActivity::class.java))
                R.id.nav_budgets -> startActivity(Intent(this, BudgetsActivity::class.java))
                R.id.nav_goals -> startActivity(Intent(this, GoalsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            Log.d(TAG, "onCreate: Drawer closed after navigation")
            true
        }

        // Initialize SwipeRefreshLayout
        setupSwipeRefreshLayout()
        Log.d(TAG, "onCreate: SwipeRefreshLayout initialized")

        // Fetch existing goals from the API
        Log.d(TAG, "onCreate: Fetching existing goals from the API")
        fetchBudgets(token)

        // Handle "Add Goal" button click
        findViewById<Button>(R.id.add_budget_button).setOnClickListener {
            Log.d(TAG, "onCreate: Add Goal button clicked")
            showAddBudgetDialog(token)
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: Back button pressed")
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "onBackPressed: Drawer is open, closing it")
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            Log.d(TAG, "onBackPressed: Drawer is closed, calling super.onBackPressed()")
            super.onBackPressed()
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout) // Initialize here

        swipeRefreshLayout.setOnRefreshListener {
            fetchbudgets(token)
        }
    }

    private fun fetchbudgets(token: String) {
        Log.d(TAG, "fetchBudgets: Fetching goals from API")

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
                call: Call<List<BudgetRead>>,
                response: Response<List<BudgetRead>>,
            ) {
                Log.d(TAG, "fetchBudgets: Received API response with code: ${response.code()}")

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val budgets = response.body() ?: run {
                        Log.e(TAG, "fetchGoals: Response body is null")
                        Toast.makeText(this@BudgetsActivity, "No goals found.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    Log.d(TAG, "fetchGoals: Goals loaded successfully: $budgets")

                    // Clear the existing goals list and add new ones
                    budgetsList.clear()
                    budgetsList.addAll(budgets)

                    // Update the UI with the new goals
                    displayBudgets(budgetsList)

                    Log.d(TAG, "fetchGoals: Displayed goals; total count: ${budgetsList.size}")
                } else {
                    Log.e(TAG, "fetchGoals: Error ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        this@budgetsActivity,
                        "Failed to retrieve goals.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<BudgetRead>>, t: Throwable) {
                Log.e(TAG, "fetchGoals: API call failed: ${t.message}", t)
                Toast.makeText(
                    this@BudgetsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    fun displayBudgets(budgets: List<BudgetRead>) {
        val budgetsContainer: LinearLayout = findViewById(R.id.budgetsContainer)
        budgetsContainer.removeAllViews() // Clear any existing views

        for (budget in budgets) {
            // Inflate the budget item layout
            val budgetView =
                LayoutInflater.from(this).inflate(R.layout.budget_item, budgetsContainer, false)

            // Set the values for the budget item
            budgetView.findViewById<TextView>(R.id.budget_amount).text = "Amount: £${budget.amount}"
            budgetView.findViewById<TextView>(R.id.budget_dates).text =
                "From: ${budget.startDate} To: ${budget.endDate}"

            val deleteButton = budgetView.findViewById<Button>(R.id.button_delete)
            deleteButton.setOnClickListener {
                deleteBudget(budget.id)
            }

            val editButton = budgetView.findViewById<Button>(R.id.button_edit)
            editButton.setOnClickListener {
                editBudget(budget.id)
            }

            // Add the budget view to the container
            budgetsContainer.addView(budgetView)
        }
    }

    private fun editBudget(budget: BudgetRead) {
        // Use the updated dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_budget, null)

        val categoryIdInput = dialogView.findViewById<EditText>(R.id.edit_budget_category_id)
        val amountInput = dialogView.findViewById<EditText>(R.id.edit_budget_amount)
        val startDateInput = dialogView.findViewById<EditText>(R.id.edit_budget_start_date)
        val endDateInput = dialogView.findViewById<EditText>(R.id.edit_budget_end_date)

        // Prepopulate the fields with current budget data
        categoryIdInput.setText(budget.categoryId.toString())
        amountInput.setText(budget.amount.toString())
        startDateInput.setText(budget.startDate)
        endDateInput.setText(budget.endDate)

        AlertDialog.Builder(this)
            .setTitle("Edit Budget")
            .setView(dialogView)
            .setPositiveButton(R.string.submit) { _, _ ->
                // Get user ID from token
                val userId = getUserIdFromToken(token)
                if (userId == -1) {
                    Toast.makeText(
                        this,
                        "Error: Invalid user ID. Cannot update budget.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Create updated budget object with user input
                val updatedBudget = BudgetCreate(
                    userId = userId,
                    categoryId = categoryIdInput.text.toString().toInt(),
                    amount = amountInput.text.toString().toDouble(),
                    startDate = startDateInput.text.toString(),
                    endDate = endDateInput.text.toString()
                )
                // Call updateBudget function to save changes
                updateBudget(budget.id, updatedBudget, token)
            }
            .setNegativeButton(R.string.back, null)
            .show()
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