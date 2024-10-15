package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import api.RetrofitClient
import api.data_class.LoginRequest
import api.data_class.TokenResponse
import com.example.personfinanceapp.R
import kotlinx.coroutines.launch
import retrofit2.Response

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
            startActivity(Intent(this, ForgotDetailsActivity::class.java)) // Go to ForgotDetailsActivity
        }

        findViewById<Button>(R.id.login).setOnClickListener {
            loginVerify() // Verify login details.
        }
    }

    // Function to verify user login.
    private fun loginVerify() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your username and password.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a LoginRequest object
        val loginRequest = LoginRequest(username = username, password = password)

        // Call the login method inside a coroutine
        lifecycleScope.launch {
            try {
                val response: Response<TokenResponse> = RetrofitClient.instance.login(loginRequest)

                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        // Store the token and proceed
                        val accessToken: String = tokenResponse.access_token // or handle as needed
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                        // Proceed to the next activity, passing the token if necessary
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.putExtra("access_token", accessToken) // Pass token to the next activity
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login successful but received an invalid response.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Login failed with status code: ${response.code()}. Response body: ${response.errorBody()?.string()}")
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
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login failed: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
