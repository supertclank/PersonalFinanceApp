package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.UserRead
import com.example.personfinanceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity for handling user login, API connection check, and navigation to other activities.
class LoginActivity : AppCompatActivity() {
    // UI components
    private lateinit var connectedCheckBox: CheckBox
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // Set the login layout.

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        connectedCheckBox = findViewById(R.id.api_connected)

        // Buttons and their actions
        findViewById<Button>(R.id.register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java)) // Go to RegisterActivity
        }

        findViewById<Button>(R.id.forgot).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ForgotDetailsActivity::class.java
                )
            ) // Go to ForgotDetailsActivity
        }

        findViewById<Button>(R.id.login).setOnClickListener {
            loginVerify() // Verify login details.
        }

        findViewById<Button>(R.id.test).setOnClickListener {
            testApiConnection() // Check API connectivity.
        }
    }

    // Function to test if API connection is active.
    private fun testApiConnection() {
        val call = RetrofitClient.instance.getUsers(0, 1)

        // Asynchronous API request to verify connection.
        call.enqueue(object : Callback<List<UserRead>> {
            override fun onResponse(
                call: Call<List<UserRead>>,
                response: Response<List<UserRead>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@LoginActivity,
                        "API connection successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    connectedCheckBox.isChecked = true // Indicate connection is established.
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "API connection failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    connectedCheckBox.isChecked = false
                }
            }

            override fun onFailure(call: Call<List<UserRead>>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    "API connection failed: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                connectedCheckBox.isChecked = false
            }
        })
    }

    // Function to verify user login.
    private fun loginVerify() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Validate input fields are not empty.
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your username and password.", Toast.LENGTH_SHORT).show()
            return
        }

        // Make API call with the username and password as separate fields.
        val call = RetrofitClient.instance.login(username = username, password = password)

        call.enqueue(object : Callback<UserRead> {
            override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                if (response.isSuccessful) {
                    // Login successful, navigate to DashboardActivity.
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    intent.putExtra("username", username) // Pass username to next activity.
                    startActivity(intent)
                    finish() // Close LoginActivity.
                } else {
                    // Handle different response codes.
                    when (response.code()) {
                        401 -> Toast.makeText(
                            this@LoginActivity,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> Toast.makeText(
                            this@LoginActivity,
                            "Login failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<UserRead>, t: Throwable) {
                // Handle network or server failure.
                Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

}
