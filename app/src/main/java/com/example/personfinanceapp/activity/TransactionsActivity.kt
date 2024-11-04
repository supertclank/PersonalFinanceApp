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
import api.data_class.BudgetRead
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
    private lateinit var transactionsList = mutableListOf<TransactionRead>()
    private lateinit var TAG = "TransactionsActivity"
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

        Log.d(TAG, "OnCreate: Fetch exisiting Transactions from the API")
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

        swipeRefreshLayout.setOnRefreshListener{
            fetchTransactions(token)
        }
    }

    private fun fetchTransactions(token: String) {
        Log.d(TAG, "fetchTransactions: Fetching transactions from the API")

        val userId = getUserIdFromToken(token)
        if(userId == -1){
            Log.e(TAG, "fetchTransactions: User ID not found")
            Toast.makeText(this, "Unable to retrieve user data", Toast.LENGTH_SHORT).show())
            return
        }

        val apiService = RetrofitClient.instance
        Log.d(TAG, "fetchTransactions: Formatted auth token")

        apiService.getTransactions(0, 10, "Bearer $token").enqueue(object : Callback<List<TransactionRead>> {
            override fun onResponse(
                call: Call<List<TransactionRead>>,
                response: Response<List<TransactionRead>>
            ) {


                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val transactions = response.body() ?: run {
                        Log.e(TAG, "fetchTransactions: Response body is null")
                        Toast.makeText(
                            this@TransactionsActivity,
                            "Unable to retrieve transactions",
                            Toast.LENGTH_SHORT)

                            .show()
                        return
                    }
                    Log.d(TAG, "fetchTransactions: Transactions retrieved successfully")

                    // Clear the existing list and add the new transactions
                    transactionsList.clear()
                    transactionsList.addAll(transactions)
                    Log.d(TAG, "fetchTransactions: Transactions list updated")

                    Log.d(TAG, "fetchTransactions: Transactions list size ${transactionsList.size}")
                    } else {
                        Log.e(TAG, "fetchTransactions: Error ${response.code()} - ${response.message()}")
                        Toast.makeText(
                            this@TransactionsActivity,
                            "Unable to retrieve transactions",
                            Toast.LENGTH_SHORT)
                            .show()
                }
            }

            override fun onFailure(call: Call<List<TransactionRead>>, t: Throwable) {
                Log.e(TAG, "fetchTransactions: API call failled: ${t.message}", t)
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


}