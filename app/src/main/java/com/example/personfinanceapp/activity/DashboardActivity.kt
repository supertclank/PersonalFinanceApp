package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import api.RetrofitClient
import api.data_class.UserRead
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var greetingTextView: TextView // TextView to display the greeting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard) // Set the layout for the dashboard

        val toolbar: Toolbar = findViewById(R.id.toolbar) // Initialize toolbar
        setSupportActionBar(toolbar) // Set the toolbar as the action bar

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        greetingTextView = findViewById(R.id.greeting_text_view) // Initialize greeting TextView

        // Set up toggle for the navigation drawer
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

        // Call function to display greeting
        displayGreeting()
    }

    // Display a greeting based on the user's first name
    private fun displayGreeting() {
        val token = TokenUtils.getTokenFromStorage(this)
        if (token != null) {
            val username = TokenUtils.decodeToken(token)?.get("username") as? String
            if (username != null) {
                fetchUserFirstName(username) // Adjusted to fetch by username
            } else {
                greetingTextView.text = "Hi, Guest!"
            }
        } else {
            greetingTextView.text = "Hi, Guest!"
        }
    }

    private fun fetchUserFirstName(username: String) {
        val apiService = RetrofitClient.instance
        apiService.getUserByUsername(username).enqueue(object : Callback<UserRead> {
            override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                if (response.isSuccessful) {
                    val firstName = response.body()?.first_name ?: "User"
                    greetingTextView.text = "Hi, $firstName!"
                } else {
                    greetingTextView.text = "Hi, Guest!"
                }
            }

            override fun onFailure(call: Call<UserRead>, t: Throwable) {
                greetingTextView.text = "Hi, Guest!"
            }
        })
    }


    // Override onBackPressed to handle drawer state
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer if it's open
        } else {
            super.onBackPressed() // Call the default behavior
        }
    }
}
