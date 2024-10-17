package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Add this import
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.personfinanceapp.R
import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard) // Ensure this matches your layout

        // Initialize toolbar (if you have a toolbar in your layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar) // Ensure you have a Toolbar in your dashboard layout
        setSupportActionBar(toolbar) // Set the toolbar as the action bar

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Set up toggle for the navigation drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, // Use the toolbar for toggle
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation item selections
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navigate to the Dashboard or Home Activity
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
                R.id.nav_transactions -> {
                    // Navigate to Transactions Activity
                    startActivity(Intent(this, TransactionsActivity::class.java))
                }
                R.id.nav_reports -> {
                    // Navigate to Reports Activity
                    startActivity(Intent(this, ReportsActivity::class.java))
                }
                R.id.nav_budgets -> {
                    // Navigate to Budgets Activity
                    startActivity(Intent(this, BudgetsActivity::class.java))
                }
                R.id.nav_settings -> {
                    // Navigate to Settings Activity
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer
            true
        }
    }

    // Override onBackPressed to handle drawer state
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed() // Call the default behavior
        }
    }
}
