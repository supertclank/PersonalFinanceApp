package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.UserCreate
import api.data_class.UserRead
import com.example.personfinanceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_1) // Set the layout for the activity

        // Initialize UI components
        val usernameField = findViewById<EditText>(R.id.username)
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val confirmPasswordField = findViewById<EditText>(R.id.confirm_password)
        val registerButton = findViewById<Button>(R.id.register)
        val backButton = findViewById<Button>(R.id.back)

        // Set a click listener for the back button to finish the current activity
        backButton.setOnClickListener {
            finish()
        }

        // Set a click listener for the register button
        registerButton.setOnClickListener {
            // Retrieve input values
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            // Validate the inputs before making the API call
            if (validateInputs(username, email, password, confirmPassword)) {
                // Create a UserCreate object with the input values
                val userCreate = UserCreate(username, email, hashed_password)
                // Make an API call to create the user
                RetrofitClient.instance.createUser(userCreate).enqueue(object : Callback<UserRead> {
                    override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                        if (response.isSuccessful) {
                            // Retrieve user ID from the response
                            val userId = response.body()?.userId?.toString()

                            if (userId != null) {
                                // Start RegisterActivity2 and pass the USER_ID
                                val intent =
                                    Intent(this@RegisterActivity, RegisterActivity2::class.java)
                                intent.putExtra("USER_ID", userId)
                                startActivity(intent)
                                finish() // Finish this activity
                            } else {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "User ID is null",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UserRead>, t: Throwable) {
                        // Handle failure of the API call
                        Toast.makeText(
                            this@RegisterActivity,
                            "Network error. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }
    }

    // Function to validate user inputs
    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Check if username is empty
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if email is empty or invalid
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Valid email is required", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate password strength
        if (!isValidPassword(password)) {
            Toast.makeText(
                this,
                "Password must contain at least 1 uppercase, 1 lowercase, 1 number, and 1 special character",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Check if passwords match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        // All checks passed
        return true
    }

    // Function to check if the password meets the requirements
    private fun isValidPassword(password: String): Boolean {
        // Regular expression to check for at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character
        val passwordPattern =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{6,}$"
        val passwordMatcher = Regex(passwordPattern)

        return passwordMatcher.matches(password) // Check if password matches the pattern
    }
}
