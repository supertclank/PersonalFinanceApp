package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personfinanceapp.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

// Activity for handling user login, API connection check, and navigation to other activities.
class LoginActivity : AppCompatActivity() {
    // UI components
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // Set the login layout.

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)

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
    }

    // Function to verify user login.
    private fun loginVerify() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_credentials), Toast.LENGTH_SHORT).show()
            return
        }

        sendLoginRequest(username, password) // Call the function to send login request
    }

    // Function to send the login request using OkHttp
    private fun sendLoginRequest(username: String, password: String) {
        val client = OkHttpClient()

        // Create the JSON body
        val json = """{"username":"$username", "password":"$password"}"""
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/login/")
            .post(requestBody)
            .build()

        // Execute the request asynchronously
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("LoginActivity", "Login failed: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        // Handle successful response
                        Log.d("LoginActivity", "Login successful: $responseBody")
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Navigate to DashboardActivity or handle the token
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e(
                            "LoginActivity",
                            "Login failed with status code: ${response.code}. Response body: ${response.body?.string()}"
                        )
                        runOnUiThread {
                            when (response.code) {
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
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Login failed: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // Function to handle error responses
    private fun handleErrorResponse(response: okhttp3.Response) {
        val errorMessage = when (response.code) {
            401 -> getString(R.string.incorrect_username_or_password)
            404 -> getString(R.string.user_not_found)
            else -> getString(R.string.login_failed_general)
        }

        runOnUiThread {
            Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }
        Log.e("LoginActivity", "Login failed with status code: ${response.code}.")
    }
}
