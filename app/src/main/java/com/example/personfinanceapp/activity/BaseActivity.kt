package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import api.RetrofitClient
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.SharedPreferenceManager
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private val TAG = "BaseActivity"
    private var isInitialCreation = true
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity_layout)

        Log.d(TAG, "onCreate called")

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        if (isInitialCreation) {
            applyUserPreferences()
            isInitialCreation = false
        }

        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)

        val dashboardView = layoutInflater.inflate(R.layout.dashboard, contentFrame, false)
        contentFrame.addView(dashboardView)

        val goalsView = layoutInflater.inflate(R.layout.goals, contentFrame, false)
        contentFrame.addView(goalsView)

        val budgetsView = layoutInflater.inflate(R.layout.budgets, contentFrame, false)
        contentFrame.addView(budgetsView)

        val reportsView = layoutInflater.inflate(R.layout.reports, contentFrame, false)
        contentFrame.addView(reportsView)

        val transactionsView = layoutInflater.inflate(R.layout.transactions, contentFrame, false)
        contentFrame.addView(transactionsView)

        val settingsView = layoutInflater.inflate(R.layout.settings, contentFrame, false)
        contentFrame.addView(settingsView)

        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Find toolbar

        if (supportActionBar == null) {
            setSupportActionBar(toolbar) // Set support action bar
        }

        // Set up toggle after setting support action bar
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation item selections
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
    }

    // Override onBackPressed to handle drawer state
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer if it's open
        } else {
            super.onBackPressed() // Call the default behavior
        }
    }

    // Apply user preferences (theme and font size)
    private fun applyUserPreferences() {
        val sharedPrefManager = SharedPreferenceManager(this, apiService = RetrofitClient.instance)
        Log.d(TAG, "Applying user preferences")

        // Apply Dark/Light Mode
        val isDarkModeEnabled = sharedPrefManager.isDarkModeEnabled()
        Log.d(TAG, "Applying dark mode: $isDarkModeEnabled")

        // Apply Font Size
        val fontSize = sharedPrefManager.getFontSize() ?: "Normal"
        Log.d(TAG, "Applying font size: $fontSize")

        // Set the theme and font size
        setAppTheme(isDarkModeEnabled)
        applyFontSize(fontSize)
    }

    private fun setAppTheme(isDarkModeEnabled: Boolean) {
        Log.d(TAG, "Setting app theme - isDarkModeEnabled: $isDarkModeEnabled")
        // Set the theme using AppCompatDelegate
        val nightMode = if (isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        // Recreate the activity to apply the theme immediately
        //recreate()
    }

    private fun applyFontSize(fontSize: String) {
        val configuration = resources.configuration
        Log.d(TAG, "Applying font size: $fontSize")
        val fontScale = when (fontSize) {
            "Small" -> 0.85f
            "Large" -> 1.15f
            else -> 1.0f // Default or "Normal"
        }
        configuration.fontScale = fontScale
        Log.d(TAG, "Font scale applied: $fontScale")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}