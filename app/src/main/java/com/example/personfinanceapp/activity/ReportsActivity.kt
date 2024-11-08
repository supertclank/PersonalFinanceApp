package com.example.personfinanceapp.activity

import android.app.AlertDialog
import android.content.Intent
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

class ReportsActivity : AppCompatActivity() {

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

        // Fetch existing Reportss from the API
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
                        Log.e(TAG, "fetcReport: Response body is null")
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

        // Fetch categories to get a map of category IDs to names
        fetchReportsTypes { categories ->
            // Log the fetched categories
            Log.d("displayReports", "Fetched categories: $categories")

            // Create a map of category IDs to category names
            val categoryMap = categories.associateBy({ it.id }, { it.name })
            Log.d("displayReports", "Category Map: $categoryMap")

            for (report in reports) {
                // Inflate the report item layout
                val reportView =
                    LayoutInflater.from(this).inflate(R.layout.report_item, reportsContainer, false)

                // Get the category name from categoryMap, or use a fallback if not found
                val name = categoryMap[report.report_type_id]
                Log.d(
                    "displayReports",
                    "Report ID: ${report.id}, Category ID: ${report.report_type_id}, Category Name: $name"
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

                // Set up edit button functionality
                val editButton = reportView.findViewById<Button>(R.id.button_edit_report)
                editButton.setOnClickListener {
                    editReport(report)
                }

                // Add the report view to the container
                reportsContainer.addView(reportView)
            }
        }
    }

    private fun fetchReportsTypes(callback: (List<ReportType>) -> Unit) {
        val apiService = RetrofitClient.instance

        // Make an API call to fetch report categories
        apiService.getReportTypes().enqueue(object : Callback<List<ReportType>> {
            override fun onResponse(
                call: Call<List<ReportType>>,
                response: Response<List<ReportType>>,
            ) {
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    callback(categories) // Pass the fetched categories to the callback
                } else {
                    // Handle error
                    Toast.makeText(
                        this@ReportsActivity,
                        "Error fetching report categories: ${response.message()}",
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

    private fun editReport(report: ReportRead) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_report, null)

        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_report_category)
        val dataInput = dialogView.findViewById<EditText>(R.id.edit_report_data)
        val generatedAtInput = dialogView.findViewById<EditText>(R.id.edit_report_date)

        // Prepopulate the fields with current report data
        // Set the spinner to the current category
        dataInput.setText(report.data.toString())
        generatedAtInput.setText(report.generated_at)

        fetchReportsTypes { categories ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, categories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            // Set the current category in the spinner
            val currentCategoryPosition =
                categories.indexOfFirst { it.id == report.report_type_id }
            categorySpinner.setSelection(currentCategoryPosition)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Report")
            .setView(dialogView)
            .setPositiveButton(R.string.submit) { _, _ ->
                // Get user ID from token
                val userId = getUserIdFromToken(token)
                if (userId == -1) {
                    Toast.makeText(
                        this,
                        "Error: Invalid user ID. Cannot update reports.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Get selected category ID from spinner
                val selectedCategoryId = categorySpinner.selectedItemId.toInt()

                // Create updated report object with user input
                val updatedReport = ReportCreate(
                    user_id = userId,
                    report_type_id = selectedCategoryId,
                    data = dataInput.text.toString().toDouble(),
                    generated_at = generatedAtInput.text.toString(),
                )
                // Call updateReport function to save changes
                updateReport(report.id, updatedReport, token)
            }
            .setNegativeButton(R.string.back, null)
            .show()
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


    private fun updateReport(reportId: Int, updatedReport: ReportCreate, token: String) {
        Log.d(TAG, "updateReport: Updating report with ID: $reportId")

        val apiService = RetrofitClient.instance
        apiService.updateReport(reportId, updatedReport, "Bearer $token")
            .enqueue(object : Callback<ReportRead> {
                override fun onResponse(call: Call<ReportRead>, response: Response<ReportRead>) {
                    Log.d(TAG, "updateReport: Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(TAG, "updateReport: Successfully updated report with ID: $reportId")
                        Toast.makeText(
                            this@ReportsActivity,
                            "report updated successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchReports(token) // Refresh the Reports list
                    } else {
                        Log.e(TAG, "updateReport: Failed to update report: ${response.message()}")
                        Toast.makeText(
                            this@ReportsActivity,
                            "Error updating report.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ReportRead>, t: Throwable) {
                    Log.e(TAG, "updateReport: API call failed: ${t.message}", t)
                    Toast.makeText(
                        this@ReportsActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
