package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import api.RetrofitClient
import api.data_class.ReportCreate
import api.data_class.ReportRead
import api.data_class.ReportType
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class ReportsActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val reportsList = mutableListOf<ReportRead>()
    private val TAG = "ReportsActivity"
    private lateinit var token: String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reports)

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

        // Fetch existing Reports from the API
        Log.d(TAG, "onCreate: Fetching existing Reports from the API")
        fetchReports(token)

        // Handle "Add report" button click
        findViewById<Button>(R.id.add_report_button).setOnClickListener {
            Log.d(TAG, "onCreate: Add report button clicked")
            showAddReportDialog(token)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
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
            fetchReports(token)
        }
    }

    private fun fetchReports(token: String) {
        Log.d(TAG, "fetchReport: Fetching Report from API")

        // Extract user ID from the token
        val userId = getUserIdFromToken(token)
        if (userId == -1) {
            Log.e(TAG, "fetchReport: Invalid user ID. Cannot fetch Report.")
            Toast.makeText(this, "Unable to retrieve user data.", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance
        Log.d(TAG, "fetchReport: Formatted auth token")

        // Start the API call
        apiService.getReports(0, 10, "Bearer $token").enqueue(object : Callback<List<ReportRead>> {
            override fun onResponse(
                call: Call<List<ReportRead>>,
                response: Response<List<ReportRead>>,
            ) {
                Log.d(TAG, "fetchReport: Received API response with code: ${response.code()}")

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val reports = response.body() ?: run {
                        Log.e(TAG, "fetchReport: Response body is null")
                        Toast.makeText(
                            this@ReportsActivity,
                            "No Reports found.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return
                    }

                    Log.d(TAG, "fetchReport: Report loaded successfully: $reports")

                    // Clear the existing Report list and add new ones
                    reportsList.clear()
                    reportsList.addAll(reports)

                    // Update the UI with the new report
                    displayReports(reportsList)

                    Log.d(TAG, "fetchReport: Displayed Report; total count: ${reportsList.size}")
                } else {
                    Log.e(TAG, "fetchReport: Error ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        this@ReportsActivity,
                        "Failed to retrieve Report.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<ReportRead>>, t: Throwable) {
                Log.e(TAG, "fetchReport: API call failed: ${t.message}", t)
                Toast.makeText(
                    this@ReportsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Stop refreshing here
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    fun displayReports(reports: List<ReportRead>) {
        val reportsContainer: LinearLayout = findViewById(R.id.reportsContainer)
        reportsContainer.removeAllViews() // Clear any existing views

        // Fetch types to get a map of types IDs to names
        fetchReportsTypes { types ->
            // Log the fetched types
            Log.d("displayReports", "Fetched types: $types")

            // Create a map of types IDs to type names
            val typesMap = types.associateBy({ it.id }, { it.name })
            Log.d("displayReports", "types Map: $typesMap")

            for (report in reports) {
                // Inflate the report item layout
                val reportView =
                    LayoutInflater.from(this).inflate(R.layout.report_item, reportsContainer, false)

                // Get the types name from typesMap, or use a fallback if not found
                val name = typesMap[report.report_type_id]
                Log.d(
                    "displayReports",
                    "Report ID: ${report.id}, types ID: ${report.report_type_id}, types Name: $name"
                )

                // Set the values for the report item
                reportView.findViewById<TextView>(R.id.report_type_value).text =
                    "Type: $name"
                reportView.findViewById<TextView>(R.id.report_data_value).text =
                    "${report.data}"
                reportView.findViewById<TextView>(R.id.report_date).text =
                    "Date: ${report.generated_at}"

                // Set up delete button functionality
                val deleteButton = reportView.findViewById<Button>(R.id.button_delete_report)
                deleteButton.setOnClickListener {
                    deleteReport(report.id)
                }

                // Add the report view to the container
                reportsContainer.addView(reportView)
            }
        }
    }

    private fun fetchReportsTypes(callback: (List<ReportType>) -> Unit) {
        val apiService = RetrofitClient.instance

        // Make an API call to fetch report types
        apiService.getReportTypes().enqueue(object : Callback<List<ReportType>> {
            override fun onResponse(
                call: Call<List<ReportType>>,
                response: Response<List<ReportType>>,
            ) {
                if (response.isSuccessful) {
                    val types = response.body() ?: emptyList()
                    callback(types) // Pass the fetched types to the callback
                } else {
                    // Handle error
                    Toast.makeText(
                        this@ReportsActivity,
                        "Error fetching report types: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<ReportType>>, t: Throwable) {
                Toast.makeText(
                    this@ReportsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun deleteReport(reportId: Int) {
        Log.d(TAG, "deleteReport: Deleting Report with ID: $reportId")

        val apiService = RetrofitClient.instance
        val call = apiService.deleteReport(reportId, "Bearer $token")

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "deleteReport: Successfully deleted report with ID: $reportId")
                    Toast.makeText(this@ReportsActivity, "Report deleted", Toast.LENGTH_SHORT)
                        .show()
                    fetchReports(token) // Refresh the reports list
                } else {
                    Log.e(
                        TAG,
                        "deleteReports: Failed to delete Report. Error code: ${response.code()}"
                    )
                    Toast.makeText(
                        this@ReportsActivity,
                        "Failed to delete report",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "deleteReports: Error deleting reports: ${t.message}", t)
                Toast.makeText(this@ReportsActivity, "Error deleting report", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun showAddReportDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_report, null)
        val reportTypeSpinner = dialogView.findViewById<Spinner>(R.id.add_report_type_spinner)

        // Declare a variable to hold report types
        val reportTypes =
            listOf("Goals Report", "Budgets Report", "Transactions Report", "Comprehensive Report")

        // Set up spinner for report types
        val reportTypeAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypes)
        reportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportTypeSpinner.adapter = reportTypeAdapter

        // Get today's date in the format yyyy-MM-dd
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(Calendar.getInstance().time)

        // Show dialog to add report
        AlertDialog.Builder(this)
            .setTitle("Add New Report")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Get the selected report type ID
                val selectedReportTypeIndex = reportTypeSpinner.selectedItemPosition
                val selectedReportTypeId = selectedReportTypeIndex + 1  // Assuming IDs start from 1

                // Create the new report data with today's date
                val newReport = ReportCreate(
                    user_id = getUserIdFromToken(token),
                    report_type_id = selectedReportTypeId,
                    generated_at = todayDate,
                    data = emptyMap()
                )

                createReport(newReport, token) // Send the new report to the backend
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createReport(newReport: ReportCreate, token: String) {
        val apiService = RetrofitClient.instance

        // Prepare the report to be created, ensuring the user_id is correctly passed
        val reportToCreate = ReportCreate(
            user_id = getUserIdFromToken(token),
            report_type_id = newReport.report_type_id,
            generated_at = newReport.generated_at,
            data = newReport.data
        )

        val call = apiService.createReport(reportToCreate, "Bearer $token")

        call.enqueue(object : Callback<ReportRead> {
            override fun onResponse(call: Call<ReportRead>, response: Response<ReportRead>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "createReport: Successfully created report: ${response.body()}")
                    Toast.makeText(
                        this@ReportsActivity,
                        "Report created successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    fetchReports(token) // Refresh the report list after creation
                } else {
                    Log.e(
                        TAG,
                        "createReport: Failed to create report. Error code: ${response.code()}"
                    )
                    Toast.makeText(
                        this@ReportsActivity,
                        "Failed to create report",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            override fun onFailure(call: Call<ReportRead>, t: Throwable) {
                Log.e(TAG, "createReport: Error creating report: ${t.message}", t)
                Toast.makeText(this@ReportsActivity, "Error creating report", Toast.LENGTH_SHORT)
                    .show()
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
