package com.example.personfinanceapp.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

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


        findViewById<Button>(R.id.logout_button).setOnClickListener {
            Log.d(TAG, "onCreate: Logout button clicked")
            TokenUtils.clearToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.account_button).setOnClickListener {
            Log.d(TAG, "onCreate: Account button clicked")
            showAccountDetailsDialog()
        }

        findViewById<Spinner>(R.id.spinner_font_size).setOnClickListener {
            fontSizeSpinner()
        }

        findViewById<Spinner>(R.id.spinner_language).setOnClickListener {
            languageSpinner()
        }



        override fun onBackPressed() {
            // Close the navigation drawer if it's open, otherwise handle the back press as usual
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun showAccountDetailsDialog() {
        val userId = getUserIdFromToken(token)
        Log.d(TAG, "showAccountDetailsDialog: User ID retrieved: $userId")

    }

    private fun fontSizeSpinner(){

    }

    private fun languageSpinner(){

    }

    private fun updateAccountDetails(userId: Int) {

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