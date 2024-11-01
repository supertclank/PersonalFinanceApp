package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import api.RetrofitClient
import api.data_class.BudgetCategory
import api.data_class.BudgetCreate
import api.data_class.BudgetRead
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
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
            Log.d(TAG, "onCreate: Drawer closed after navigation")
            true
        }

        // Initialize SwipeRefreshLayout
        setupSwipeRefreshLayout()
        Log.d(TAG, "onCreate: SwipeRefreshLayout initialized")

        // Fetch existing Budgets from the API
        Log.d(TAG, "onCreate: Fetching existing Budgets from the API")
        fetchBudgets(token)

        // Handle "Add Budget" button click
        findViewById<Button>(R.id.add_budget_button).setOnClickListener {
            Log.d(TAG, "onCreate: Add Budget button clicked")
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
            fetchBudgets(token)
        }
    }

    private fun fetchBudgets(token: String) {
        Log.d(TAG, "fetchBudgets: Fetching budgets from API")

        // Extract user ID from the token
        val userId = getUserIdFromToken(token)
        if (userId == -1) {
            Log.e(TAG, "fetchBudgets: Invalid user ID. Cannot fetch Budgets.")
            Toast.makeText(this, "Unable to retrieve user data.", Toast.LENGTH_SHORT).show()
            // Optionally stop refreshing here if you have a SwipeRefreshLayout
            return
        }

        val apiService = RetrofitClient.instance
        Log.d(TAG, "fetchBudgets: Formatted auth token")

        // Start the API call
        apiService.getBudgets(0, 10, "Bearer $token").enqueue(object : Callback<List<BudgetRead>> {
            override fun onResponse(
                call: Call<List<BudgetRead>>,
                response: Response<List<BudgetRead>>,
            ) {
                Log.d(TAG, "fetchBudgets: Received API response with code: ${response.code()}")

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val budgets = response.body() ?: run {
                        Log.e(TAG, "fetchBudget: Response body is null")
                        Toast.makeText(
                            this@BudgetsActivity,
                            "No Budgets found.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return
                    }

                    Log.d(TAG, "fetchBudgets: Budgets loaded successfully: $budgets")

                    // Clear the existing Budgets list and add new ones
                    budgetsList.clear()
                    budgetsList.addAll(budgets)

                    // Update the UI with the new Budgets
                    displayBudgets(budgetsList)

                    Log.d(TAG, "fetchBudgets: Displayed Budgets; total count: ${budgetsList.size}")
                } else {
                    Log.e(TAG, "fetchBudgets: Error ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        this@BudgetsActivity,
                        "Failed to retrieve Budgets.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<BudgetRead>>, t: Throwable) {
                Log.e(TAG, "fetchBudgets: API call failed: ${t.message}", t)
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

    fun displayBudgets(budget: List<BudgetRead>) {
        val budgetsContainer: LinearLayout = findViewById(R.id.budgetsContainer)
        budgetsContainer.removeAllViews() // Clear any existing views

        for (budget in budget) {
            // Inflate the budget item layout
            val budgetView =
                LayoutInflater.from(this).inflate(R.layout.budget_item, budgetsContainer, false)

            // Set the values for the budget item
            budgetView.findViewById<TextView>(R.id.budget_category_title).text =
                "Category ID: ${budget.budgetCategoryId}"
            budgetView.findViewById<TextView>(R.id.budget_amount_value).text =
                "Amount: £${budget.amount}"
            budgetView.findViewById<TextView>(R.id.budget_start_date).text =
                "Start Date: ${budget.startDate}"
            budgetView.findViewById<TextView>(R.id.budget_end_date).text =
                "End Date: ${budget.endDate}"

            // Set up delete button functionality
            val deleteButton = budgetView.findViewById<Button>(R.id.button_delete_budget)
            deleteButton.setOnClickListener {
                deleteBudget(budget.budgetId)
            }

            // Set up edit button functionality
            val editButton = budgetView.findViewById<Button>(R.id.button_edit_budget)
            editButton.setOnClickListener {
                editBudget(budget)
            }

            // Add the budget view to the container
            budgetsContainer.addView(budgetView)
        }
    }

    private fun fetchBudgetCategories(callback: (List<BudgetCategory>) -> Unit) {
        val apiService = RetrofitClient.instance

        // Make an API call to fetch budget categories
        apiService.getBudgetCategories().enqueue(object : Callback<List<BudgetCategory>> {
            override fun onResponse(
                call: Call<List<BudgetCategory>>,
                response: Response<List<BudgetCategory>>,
            ) {
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    callback(categories) // Pass the fetched categories to the callback
                } else {
                    // Handle error
                    Toast.makeText(
                        this@BudgetsActivity,
                        "Error fetching budget categories: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<BudgetCategory>>, t: Throwable) {
                Toast.makeText(
                    this@BudgetsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun populateCategorySpinner(dialogView: View, categories: List<BudgetCategory>) {
        val categorySpinner: Spinner = dialogView.findViewById(R.id.add_budget_category_spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun editBudget(budget: BudgetRead) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_budget, null)

        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_budget_category)
        val amountInput = dialogView.findViewById<EditText>(R.id.edit_budget_amount)
        val startDateInput = dialogView.findViewById<EditText>(R.id.edit_budget_start_date)
        val endDateInput = dialogView.findViewById<EditText>(R.id.edit_budget_end_date)

        // Prepopulate the fields with current budget data
        // Set the spinner to the current category
        amountInput.setText(budget.amount.toString())
        startDateInput.setText(budget.startDate)
        endDateInput.setText(budget.endDate)

        fetchBudgetCategories { categories ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, categories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            // Set the current category in the spinner
            val currentCategoryPosition = categories.indexOfFirst { it.id == budget.budgetCategoryId }
            categorySpinner.setSelection(currentCategoryPosition)
        }

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

                // Get selected category ID from spinner
                val selectedCategoryId = categorySpinner.selectedItemId.toInt()

                // Create updated budget object with user input
                val updatedBudget = BudgetCreate(
                    userId = userId,
                    budgetCategoryId = selectedCategoryId,
                    amount = amountInput.text.toString().toDouble(),
                    startDate = startDateInput.text.toString(),
                    endDate = endDateInput.text.toString()
                )
                // Call updateBudget function to save changes
                updateBudget(budget.budgetId, updatedBudget, token)
            }
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun deleteBudget(budgetId: Int) {
        Log.d(TAG, "deleteBudget: Deleting budget with ID: $budgetId")

        val apiService = RetrofitClient.instance
        val call = apiService.deleteBudget(budgetId, "Bearer $token")

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "deleteBudget: Successfully deleted budget with ID: $budgetId")
                    Toast.makeText(this@BudgetsActivity, "Budget deleted", Toast.LENGTH_SHORT)
                        .show()
                    fetchBudgets(token) // Refresh the budgets list
                } else {
                    Log.e(
                        TAG,
                        "deleteBudget: Failed to delete budget. Error code: ${response.code()}"
                    )
                    Toast.makeText(
                        this@BudgetsActivity,
                        "Failed to delete budget",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "deleteBudget: Error deleting budget: ${t.message}", t)
                Toast.makeText(this@BudgetsActivity, "Error deleting budget", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun updateBudget(budgetId: Int, updatedBudget: BudgetCreate, token: String) {
        Log.d(TAG, "updateBudget: Updating budget with ID: $budgetId")

        val apiService = RetrofitClient.instance
        apiService.updateBudget(budgetId, updatedBudget, "Bearer $token")
            .enqueue(object : Callback<BudgetRead> {
                override fun onResponse(call: Call<BudgetRead>, response: Response<BudgetRead>) {
                    Log.d(TAG, "updateBudget: Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(TAG, "updateBudget: Successfully updated budget with ID: $budgetId")
                        Toast.makeText(
                            this@BudgetsActivity,
                            "Budget updated successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchBudgets(token) // Refresh the budgets list
                    } else {
                        Log.e(TAG, "updateBudget: Failed to update budget: ${response.message()}")
                        Toast.makeText(
                            this@BudgetsActivity,
                            "Error updating budget.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BudgetRead>, t: Throwable) {
                    Log.e(TAG, "updateBudget: API call failed: ${t.message}", t)
                    Toast.makeText(
                        this@BudgetsActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showAddBudgetDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.add_budget_amount)
        val startDateInput = dialogView.findViewById<EditText>(R.id.add_budget_start_date)
        val endDateInput = dialogView.findViewById<EditText>(R.id.add_budget_end_date)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.add_budget_category_spinner)

        // Declare a variable to hold the categories
        var categories: List<BudgetCategory> = emptyList()

        // Fetch categories and set up the spinner
        fetchBudgetCategories { fetchedCategories ->
            categories = fetchedCategories // Assign fetched categories to the variable
            if (categories.isNotEmpty()) {
                val categoryNames = categories.map { it.name }
                val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = categoryAdapter
            } else {
                // Handle the case where there are no categories
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up DatePickerDialogs for start and end dates
        startDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                startDateInput.setText(formattedDate)
            }, year, month, day).show()
        }

        endDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                endDateInput.setText(formattedDate)
            }, year, month, day).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Budget")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Validate input fields
                val amountStr = amountInput.text.toString()
                val startDate = startDateInput.text.toString()
                val endDate = endDateInput.text.toString()

                if (amountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Ensure categories were fetched
                if (categories.isEmpty()) {
                    Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Get the selected category ID
                val selectedCategoryIndex = categorySpinner.selectedItemPosition
                val selectedCategoryId = categories[selectedCategoryIndex].id

                // Create the new budget
                val newBudget = BudgetCreate(
                    userId = getUserIdFromToken(token),
                    budgetCategoryId = selectedCategoryId,
                    amount = amountStr.toDouble(),
                    startDate = startDate,
                    endDate = endDate
                )
                createBudget(newBudget, token) // Create budget and refresh list
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createBudget(newBudget: BudgetCreate, token: String) {
        val apiService = RetrofitClient.instance

        // Make sure to pass the userId from the token correctly
        val budgetToCreate = BudgetCreate(
            userId = getUserIdFromToken(token),
            budgetCategoryId = newBudget.budgetCategoryId,
            amount = newBudget.amount,
            startDate = newBudget.startDate,
            endDate = newBudget.endDate
        )

        val call = apiService.createBudget(budgetToCreate, "Bearer $token")

        call.enqueue(object : Callback<BudgetRead> {
            override fun onResponse(call: Call<BudgetRead>, response: Response<BudgetRead>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "createBudget: Successfully created budget: ${response.body()}")
                    Toast.makeText(this@BudgetsActivity, "Budget created", Toast.LENGTH_SHORT).show()
                    fetchBudgets(token) // Refresh the budget list
                } else {
                    Log.e(TAG, "createBudget: Failed to create budget. Error code: ${response.code()}")
                    Toast.makeText(this@BudgetsActivity, "Failed to create budget", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BudgetRead>, t: Throwable) {
                Log.e(TAG, "createBudget: Error creating budget: ${t.message}", t)
                Toast.makeText(this@BudgetsActivity, "Error creating budget", Toast.LENGTH_SHORT).show()
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