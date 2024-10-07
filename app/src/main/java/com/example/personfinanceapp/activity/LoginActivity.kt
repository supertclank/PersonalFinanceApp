package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.LoginRequest
import api.data_class.UserRead
import com.example.personfinanceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var connectedCheckBox: CheckBox
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        connectedCheckBox = findViewById(R.id.api_connected)

        val registerButton = findViewById<Button>(R.id.register)
        val forgotButton = findViewById<Button>(R.id.forgot)
        val loginButton = findViewById<Button>(R.id.login)
        val testButton = findViewById<Button>(R.id.test)

        // Set button click listeners
        registerButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        forgotButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotDetailsActivity::class.java))
        }

        loginButton.setOnClickListener {
            loginVerify()
        }

        testButton.setOnClickListener {
            testApiConnection()
        }
    }

    // Function to test API connection
    private fun testApiConnection() {
        val call = RetrofitClient.instance.getUsers(0, 1)
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
                    connectedCheckBox.isChecked = true
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

    // Function to verify login
    private fun loginVerify() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your username and password.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Create a login request object
        val loginRequest = LoginRequest(username, password)

        // Call the login endpoint
        val call = RetrofitClient.instance.login(loginRequest)

        call.enqueue(object : Callback<UserRead> {
            override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                if (response.isSuccessful) {
                    // Handle successful login
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT)
                        .show()
                    // Navigate to another activity if necessary
                } else {
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
                Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
