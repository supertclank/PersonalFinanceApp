package com.example.personfinanceapp.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import api.RetrofitClient
import api.data_class.UserRead
import api.data_class.UserUpdate
import com.auth0.android.jwt.JWT
import com.example.personfinanceapp.R
import com.example.personfinanceapp.utils.SharedPreferenceManager
import com.example.personfinanceapp.utils.TokenUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : BaseActivity() {

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
            showAccountDetailsDialog(token)
        }

        fontSizeSpinner()

        val darkModeSwitch: SwitchMaterial = findViewById(R.id.dark_mode)

        // Set up the listener to toggle dark mode when the switch is changed
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "onCreate: Dark mode switch changed to $isChecked")
            if (isChecked) {
                enableDarkMode(this)  // Enable dark mode
            } else {
                enableLightMode(this)  // Enable light mode
            }
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

    private fun showAccountDetailsDialog(token: String) {
        Log.d(TAG, "showAccountDetailsDialog: Token received: $token")

        val userId = getUserIdFromToken(token)
        if (userId == null) {
            Log.e(TAG, "showAccountDetailsDialog: Failed to extract User ID from token.")
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "showAccountDetailsDialog: Extracted User ID: $userId")

        val apiService = RetrofitClient.instance
        apiService.getUser(userId).enqueue(object : Callback<UserRead> {
            override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                if (response.isSuccessful) {
                    val userRead = response.body()
                    if (userRead != null) {
                        displayUserDetailsDialog(userRead, token)
                    } else {
                        Log.e(TAG, "showAccountDetailsDialog: Received null user data.")
                        Toast.makeText(
                            this@SettingsActivity,
                            "Failed to load account details. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        TAG,
                        "showAccountDetailsDialog: API response failed. Code: ${response.code()}"
                    )
                    Toast.makeText(
                        this@SettingsActivity,
                        "Error retrieving account details. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserRead>, t: Throwable) {
                Log.e(TAG, "showAccountDetailsDialog: Network error", t)
                Toast.makeText(
                    this@SettingsActivity,
                    "Network error. Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayUserDetailsDialog(userRead: UserRead, token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_details, null)

        val firstNameTextView = dialogView.findViewById<TextView>(R.id.account_first_name)
        val lastNameTextView = dialogView.findViewById<TextView>(R.id.account_last_name)
        val emailTextView = dialogView.findViewById<TextView>(R.id.account_email)
        val phoneNumberTextView = dialogView.findViewById<TextView>(R.id.account_phone_number)
        val editButton = dialogView.findViewById<Button>(R.id.edit_account_button)

        firstNameTextView.text = "First Name: ${userRead.first_name}"
        lastNameTextView.text = "Last Name: ${userRead.last_name}"
        emailTextView.text = "Email: ${userRead.email}"
        phoneNumberTextView.text = "Phone Number: ${userRead.phone_number}"

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Account Details")
            .setPositiveButton("Close", null)
            .create()

        editButton.setOnClickListener {
            dialogBuilder.dismiss()
            showEditAccountDetailsDialog(userRead, token)
        }

        dialogBuilder.show()
    }

    private fun showEditAccountDetailsDialog(userRead: UserRead, token: String) {
        val editDialogView = layoutInflater.inflate(R.layout.dialog_edit_account_details, null)

        val editFirstName = editDialogView.findViewById<EditText>(R.id.edit_first_name)
        val editLastName = editDialogView.findViewById<EditText>(R.id.edit_last_name)
        val editEmail = editDialogView.findViewById<EditText>(R.id.edit_email)
        val editPhoneNumber = editDialogView.findViewById<EditText>(R.id.edit_phone_number)
        val saveButton = editDialogView.findViewById<Button>(R.id.save_changes_button)

        editFirstName.setText(userRead.first_name)
        editLastName.setText(userRead.last_name)
        editEmail.setText(userRead.email)
        editPhoneNumber.setText(userRead.phone_number)

        val editDialogBuilder = AlertDialog.Builder(this)
            .setView(editDialogView)
            .setTitle("Edit Account Details")
            .setPositiveButton("Cancel", null)
            .create()

        saveButton.setOnClickListener {
            val updatedUser = userRead.copy(
                first_name = editFirstName.text.toString(),
                last_name = editLastName.text.toString(),
                email = editEmail.text.toString(),
                phone_number = editPhoneNumber.text.toString()
            )

            updateAccountDetails(userRead.id, updatedUser, token)
            editDialogBuilder.dismiss()
        }

        editDialogBuilder.show()
    }

    private fun updateAccountDetails(userId: Int, updatedUser: UserRead, token: String) {
        // Create UserUpdate object with only updatable fields
        val userUpdate = UserUpdate(
            username = updatedUser.username,
            first_name = updatedUser.first_name,
            last_name = updatedUser.last_name,
            phone_number = updatedUser.phone_number,
            email = updatedUser.email
        )

        // Make an API call to update user details
        val apiService = RetrofitClient.instance
        apiService.updateUser(userId, userUpdate, "bearer $token")
            .enqueue(object : Callback<UserRead> {
                override fun onResponse(
                    call: Call<UserRead>,
                    response: Response<UserRead>,
                ) {
                    Log.d(TAG, "updateAccountDetails: Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        // Display updated details
                        showAccountDetailsDialog(token)
                        Toast.makeText(
                            this@SettingsActivity,
                            "Account updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(TAG, "updateAccountDetails: Failed to update user details")
                        Toast.makeText(
                            this@SettingsActivity,
                            "Failed to update account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserRead>, t: Throwable) {
                    Log.e(TAG, "updateAccountDetails: Error updating user details", t)
                    Toast.makeText(
                        this@SettingsActivity,
                        "Error updating account",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun fontSizeSpinner() {
        val fontSizeOptions = arrayOf("Large", "Normal", "Small")
        val fontSizeSpinner = findViewById<Spinner>(R.id.font_size_spinner)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fontSizeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fontSizeSpinner.adapter = adapter

        val sharedPrefManager = SharedPreferenceManager(this, apiService = RetrofitClient.instance)
        val savedFontSize = sharedPrefManager.getFontSize() ?: "Normal"
        fontSizeSpinner.setSelection(fontSizeOptions.indexOf(savedFontSize))

        // Log the saved font size
        Log.d("FontSize", "Saved font size: $savedFontSize")

        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                // Check if the view is null before using it
                if (view != null) {
                    val selectedFontSize = fontSizeOptions[position]
                    sharedPrefManager.saveFontSize(selectedFontSize)
                    adjustFontSize(selectedFontSize)

                    // Log the font size selected by the user
                    Log.d("FontSize", "User selected font size: $selectedFontSize")
                } else {
                    // Handle the case where view is null
                    Log.e("FontSize", "View is null in onItemSelected")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                val defaultFontSize = "Normal"
                fontSizeSpinner.setSelection(fontSizeOptions.indexOf(defaultFontSize))
                sharedPrefManager.saveFontSize(defaultFontSize)
                adjustFontSize(defaultFontSize)

                // Log the default font size selection
                Log.d("FontSize", "No selection, defaulting to font size: $defaultFontSize")
            }
        }
    }

    private fun adjustFontSize(fontSize: String) {
        val baseTextSize = 16f

        val newSize = when (fontSize) {
            "Large" -> baseTextSize * 1.25f
            "Small" -> baseTextSize * 0.85f
            else -> baseTextSize
        }

        // Log the adjusted font size
        Log.d("FontSize", "Adjusted font size: $newSize for $fontSize")

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                getAllViews(rootView).forEach { view ->
                    when (view) {
                        is TextView -> view.textSize = newSize
                        is Button -> view.textSize = newSize
                        is EditText -> view.textSize = newSize
                    }
                }
            }
        })
    }

    private fun getAllViews(v: View): List<View> {
        val currentActivity = v.context as? Activity // Get the context of the view

        // Check if the context is an Activity and belongs to excluded activities
        if (currentActivity != null && (
                    currentActivity is LoginActivity ||
                            currentActivity is RegisterActivity ||
                            currentActivity is ForgotDetailsActivity
                    )
        ) {
            return emptyList() // Exclude these activities from traversal
        }

        if (v !is ViewGroup) {
            return listOf(v)
        }

        val result = mutableListOf<View>()
        for (i in 0 until v.childCount) {
            result.addAll(getAllViews(v.getChildAt(i)))
        }
        result.add(v)
        return result
    }

    private fun updateFontSizeRecursively(view: View, fontSize: Int) {
        if (view is TextView) {
            view.textSize = fontSize.toFloat()
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateFontSizeRecursively(view.getChildAt(i), fontSize)
            }
        }
    }

    private fun enableDarkMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("dark_mode", true)
        editor.apply()
    }

    private fun enableLightMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("dark_mode", false)
        editor.apply()
    }

    private fun getUserIdFromToken(token: String): Int {
        return try {
            val jwt = JWT(token)
            val userId = jwt.getClaim("id").asInt() ?: -1
            Log.d(TAG, "getUserIdFromToken: User ID found: $userId")
            userId // Return the value after logging
        } catch (e: Exception) {
            Log.e(TAG, "getUserIdFromToken: Error decoding token: ${e.message}")
            -1
        }
    }
}