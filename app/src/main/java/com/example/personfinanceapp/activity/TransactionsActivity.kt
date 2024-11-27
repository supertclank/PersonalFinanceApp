package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import api.data_class.TransactionCategory
import api.data_class.TransactionCreate
import api.data_class.TransactionRead
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var transactionsList = mutableListOf<TransactionRead>()
    private var TAG = "TransactionsActivity"
    private lateinit var token: String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transactions)

        Log.d(TAG, "onCreate: Initializing the activity")

        token = TokenUtils.getTokenFromStorage(this) ?: run {
            Log.e(TAG, "onCreate: Token is null")
            Toast.makeText(this, "Token is null", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "onCreate: Token retrieved $token")

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Initialize the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        Log.d(TAG, "onCreate: Drawer layout initialized")

        // Set up navigation view item selection listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            Log.d(TAG, "onCreate: Navigation item selected ${menuItem.itemId}")
            // Handle navigation view item selection
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
            Log.d(TAG, "OnCreate: Drawer closed after navigation")
            true
        }
        setupSwipeRefreshLayout()
        Log.d(TAG, "onCreate: Swipe refresh layout initialized")

        Log.d(TAG, "OnCreate: Fetch existing Transactions from the API")
        fetchTransactions(token)

        findViewById<Button>(R.id.add_transaction_button).setOnClickListener {
            Log.d(TAG, "onCreate: Add transaction button clicked")
            showAddTransactionDialog(token)

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

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)

        swipeRefreshLayout.setOnRefreshListener {
            fetchTransactions(token)
        }
    }

    private fun fetchTransactions(token: String) {
        Log.d(TAG, "fetchTransactions: Fetching transactions from the API")

        val userId = getUserIdFromToken(token)
        if (userId == -1) {
            Log.e(TAG, "fetchTransactions: User ID not found")
            Toast.makeText(this, "Unable to retrieve user data", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance
        Log.d(TAG, "fetchTransactions: Formatted auth token")

        apiService.getTransactions(0, 10, "Bearer $token")
            .enqueue(object : Callback<List<TransactionRead>> {
                override fun onResponse(
                    call: Call<List<TransactionRead>>,
                    response: Response<List<TransactionRead>>,
                ) {


                    swipeRefreshLayout.isRefreshing = false

                    if (response.isSuccessful) {
                        val transactions = response.body() ?: run {
                            Log.e(TAG, "fetchTransactions: Response body is null")
                            Toast.makeText(
                                this@TransactionsActivity,
                                "Unable to retrieve transactions",
                                Toast.LENGTH_SHORT
                            )

                                .show()
                            return
                        }
                        Log.d(TAG, "fetchTransactions: Transactions retrieved successfully")

                        // Clear the existing list and add the new transactions
                        transactionsList.clear()
                        transactionsList.addAll(transactions)
                        Log.d(TAG, "fetchTransactions: Transactions list updated")

                        displayTransactions(transactionsList)

                        Log.d(
                            TAG,
                            "fetchTransactions: Transactions list size ${transactionsList.size}"
                        )
                    } else {
                        Log.e(
                            TAG,
                            "fetchTransactions: Error ${response.code()} - ${response.message()}"
                        )
                        Toast.makeText(
                            this@TransactionsActivity,
                            "Unable to retrieve transactions",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                override fun onFailure(call: Call<List<TransactionRead>>, t: Throwable) {
                    Log.e(TAG, "fetchTransactions: API call failed: ${t.message}", t)
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    swipeRefreshLayout.isRefreshing = false
                }
            })
    }

    fun displayTransactions(transactions: List<TransactionRead>) {
        val transactionsContainer: LinearLayout = findViewById(R.id.transactionsContainer)
        transactionsContainer.removeAllViews() // clear any existing views

        // Fetch categories to get a map of category IDs to names
        fetchTransactionCategories { categories ->
            // Log the fetched categories
            Log.d(TAG, "displayTransactions: Categories fetched")

            // Create a map of category IDs to names
            val categoryMap = categories.associateBy({ it.id }, { it.name })
            Log.d(TAG, "displayTransactions, Categories mapped: $categoryMap")

            for (transaction in transactions) {
                val transactionView =
                    LayoutInflater.from(this)
                        .inflate(R.layout.transaction_item, transactionsContainer, false)

                val name = categoryMap[transaction.transaction_category_id]
                Log.d(
                    TAG,
                    "displayTransactions: Transaction ID: ${transaction.id}, Category ID: ${transaction.transaction_category_id}, Name: $name"
                )

                transactionView.findViewById<TextView>(R.id.transaction_description).text =
                    "${transaction.description}"
                transactionView.findViewById<TextView>(R.id.transaction_category).text =
                    "${name}"
                transactionView.findViewById<TextView>(R.id.transaction_date).text =
                    "£${transaction.date}"
                transactionView.findViewById<TextView>(R.id.transaction_amount).text =
                    "${transaction.amount}"

                val deleteButton =
                    transactionView.findViewById<Button>(R.id.button_delete)
                deleteButton.setOnClickListener {
                    deleteTransaction(transaction.id)
                }

                val editButton = transactionView.findViewById<Button>(R.id.button_edit)
                editButton.setOnClickListener {
                    editTransaction(transaction)
                }

                transactionsContainer.addView(transactionView)
            }
        }

    }

    private fun fetchTransactionCategories(callback: (List<TransactionCategory>) -> Unit) {
        val apiService = RetrofitClient.instance

        // Make an API call to fetch Transaction categories
        apiService.getTransactionCategories().enqueue(object : Callback<List<TransactionCategory>> {
            override fun onResponse(
                call: Call<List<TransactionCategory>>,
                response: Response<List<TransactionCategory>>,
            ) {
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    callback(categories) // Pass the fetched categories to the callback
                } else {
                    // Handle error
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Error fetching Transaction categories: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<TransactionCategory>>, t: Throwable) {
                Toast.makeText(
                    this@TransactionsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun editTransaction(Transaction: TransactionRead) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_transaction, null)

        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_transaction_category)
        val amountInput = dialogView.findViewById<EditText>(R.id.edit_transaction_amount)
        val dateInput = dialogView.findViewById<EditText>(R.id.edit_transaction_date)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.edit_transaction_description)

        // Prepopulate the fields with current transaction data
        // Set the spinner to the current category
        amountInput.setText(Transaction.amount.toString())
        dateInput.setText(Transaction.date)
        descriptionInput.setText(Transaction.description)

        fetchTransactionCategories { categories ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, categories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            // Set the current category in the spinner
            val currentCategoryPosition =
                categories.indexOfFirst { it.id == Transaction.transaction_category_id }
            categorySpinner.setSelection(currentCategoryPosition)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Transaction")
            .setView(dialogView)
            .setPositiveButton(R.string.submit) { _, _ ->
                // Get user ID from token
                val userId = getUserIdFromToken(token)
                if (userId == -1) {
                    Toast.makeText(
                        this,
                        "Error: Invalid user ID. Cannot update transaction.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Get selected category ID from spinner
                val selectedCategoryId = categorySpinner.selectedItemId.toInt()

                // Create updated transaction object with user input
                val updatedTransaction = TransactionCreate(
                    user_id = userId,
                    transaction_category_id = selectedCategoryId,
                    amount = amountInput.text.toString().toDouble(),
                    date = dateInput.text.toString(),
                    description = descriptionInput.text.toString()
                )
                // Call updateTransaction function to save changes
                updateTransaction(Transaction.id, updatedTransaction, token)
            }
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun deleteTransaction(transactionId: Int) {
        Log.d(TAG, "deleteTransaction: Deleting transaction with ID: $transactionId")

        val apiService = RetrofitClient.instance
        val call = apiService.deleteTransaction(transactionId, "Bearer $token")

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(
                        TAG,
                        "deleteTransaction: Successfully deleted transaction with ID: $transactionId"
                    )
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Transaction deleted",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    fetchTransactions(token) // Refresh the transactions list
                } else {
                    Log.e(
                        TAG,
                        "deleteTransaction: Failed to delete transaction. Error code: ${response.code()}"
                    )
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Failed to delete transaction",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "deleteTransaction: Error deleting transaction: ${t.message}", t)
                Toast.makeText(
                    this@TransactionsActivity,
                    "Error deleting transaction",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun updateTransaction(
        transactionId: Int,
        updatedTransaction: TransactionCreate,
        token: String,
    ) {
        Log.d(TAG, "updateTransaction: Updating transaction with ID: $transactionId")

        val apiService = RetrofitClient.instance
        apiService.updateTransaction(transactionId, updatedTransaction, "Bearer $token")
            .enqueue(object : Callback<TransactionRead> {
                override fun onResponse(
                    call: Call<TransactionRead>,
                    response: Response<TransactionRead>,
                ) {
                    Log.d(TAG, "updateTransaction: Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(
                            TAG,
                            "updateTransaction: Successfully updated transaction with ID: $transactionId"
                        )
                        Toast.makeText(
                            this@TransactionsActivity,
                            "Transaction updated successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchTransactions(token) // Refresh the transactions list
                    } else {
                        Log.e(
                            TAG,
                            "updateTransaction: Failed to update transaction: ${response.message()}"
                        )
                        Toast.makeText(
                            this@TransactionsActivity,
                            "Error updating transaction.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TransactionRead>, t: Throwable) {
                    Log.e(TAG, "updateTransaction: API call failed: ${t.message}", t)
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showAddTransactionDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        val categorySpinner =
            dialogView.findViewById<Spinner>(R.id.add_spinner_transaction_category)
        val amountInput = dialogView.findViewById<EditText>(R.id.add_transaction_amount)
        val dateInput = dialogView.findViewById<EditText>(R.id.add_transaction_date)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.add_transaction_description)

        // Declare a variable to hold the categories
        var categories: List<TransactionCategory> = emptyList()

        // Fetch categories and set up the spinner
        fetchTransactionCategories { fetchedCategories ->
            categories = fetchedCategories // Assign fetched categories to the variable
            if (categories.isNotEmpty()) {
                val categoryNames = categories.map { it.name }
                val categoryAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = categoryAdapter
            } else {
                // Handle the case where there are no categories
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up DatePickerDialogs for start and end dates
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateInput.setText(formattedDate)
            }, year, month, day).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Transaction")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Validate input fields
                val amountStr = amountInput.text.toString()
                val date = dateInput.text.toString()
                val descriptionStr = descriptionInput.text.toString()

                if (amountStr.isEmpty() || date.isEmpty()) {
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

                // Create the new transaction
                val newTransaction = TransactionCreate(
                    user_id = getUserIdFromToken(token),
                    transaction_category_id = selectedCategoryId,
                    amount = amountStr.toDouble(),
                    date = date,
                    description = descriptionStr
                )
                createTransaction(newTransaction, token) // Create transaction and refresh list
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createTransaction(newTransaction: TransactionCreate, token: String) {
        val apiService = RetrofitClient.instance

        // Make sure to pass the userId from the token correctly
        val transactionToCreate = TransactionCreate(
            user_id = getUserIdFromToken(token),
            transaction_category_id = newTransaction.transaction_category_id,
            amount = newTransaction.amount,
            date = newTransaction.date,
            description = newTransaction.description
        )

        val call = apiService.createTransaction(transactionToCreate, "Bearer $token")

        call.enqueue(object : Callback<TransactionRead> {
            override fun onResponse(
                call: Call<TransactionRead>,
                response: Response<TransactionRead>,
            ) {
                if (response.isSuccessful) {
                    Log.d(
                        TAG,
                        "createTransaction: Successfully created Transaction: ${response.body()}"
                    )
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Transaction created",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchTransactions(token) // Refresh the Transaction list
                } else {
                    Log.e(
                        TAG,
                        "createTransaction: Failed to create transaction. Error code: ${response.code()}"
                    )
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Failed to create transaction",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<TransactionRead>, t: Throwable) {
                Log.e(TAG, "createTransaction: Error creating Transaction: ${t.message}", t)
                Toast.makeText(
                    this@TransactionsActivity,
                    "Error creating transaction",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getUserIdFromToken(token: String): Int {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("id").asInt() ?: -1 // Return -1 if user ID not found
            Log.d(TAG, "getUserIdFromToken: User ID found: ${jwt.getClaim("id").asInt()}")
        } catch (e: Exception) {
            Log.e(TAG, "getUserIdFromToken: Error decoding token: ${e.message}")
            -1
        }
    }

}