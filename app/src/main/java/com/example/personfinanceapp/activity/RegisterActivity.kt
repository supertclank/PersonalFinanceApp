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
        setContentView(R.layout.register_1)

        val backButton = findViewById<Button>(R.id.back)
        val firstNameField = findViewById<EditText>(R.id.first_name)
        val surnameField = findViewById<EditText>(R.id.surname)
        val phoneNumberField = findViewById<EditText>(R.id.phone_number)
        val usernameField = findViewById<EditText>(R.id.username)
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val confirmPasswordField = findViewById<EditText>(R.id.confirm_password)
        val registerButton = findViewById<Button>(R.id.register)

        backButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val firstName = firstNameField.text.toString()
            val surname = surnameField.text.toString()
            val phoneNumber = phoneNumberField.text.toString()
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            // Perform validation
            if (validateInputs(firstName, surname, phoneNumber, username, email, password, confirmPassword)) {
                val userCreate = UserCreate(username, email, password)
                RetrofitClient.instance.createUser(userCreate).enqueue(object : Callback<UserRead> {
                    override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                        if (response.isSuccessful) {
                            // Navigate to LoginActivity if registration is successful
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish() // Close the RegisterActivity
                        } else {
                            // Show an error message
                            Toast.makeText(this@RegisterActivity, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<UserRead>, t: Throwable) {
                        // Handle network failure (e.g., show an error message)
                        Toast.makeText(this@RegisterActivity, "Network error. Please try again.", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    // Function to validate inputs
    private fun validateInputs(firstName: String, surname: String, phoneNumber: String, username: String, email: String, password: String, confirmPassword: String): Boolean {
        // Check if first name is empty or contains non-alphabet characters
        if (firstName.isEmpty() || !firstName.matches(Regex("^[A-Za-z]+$"))) {
            Toast.makeText(this, "First name is required and must contain only alphabetic characters", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if surname is empty or contains non-alphabet characters
        if (surname.isEmpty() || !surname.matches(Regex("^[A-Za-z]+$"))) {
            Toast.makeText(this, "Surname is required and must contain only alphabetic characters", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if phone number is empty or contains non-digit characters
        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("^[0-9]{10,15}$"))) {
            Toast.makeText(this, "Valid phone number is required (10 to 15 digits)", Toast.LENGTH_SHORT).show()
            return false
        }

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
            Toast.makeText(this, "Password must contain at least 1 uppercase, 1 lowercase, 1 number, and 1 special character", Toast.LENGTH_LONG).show()
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
        // Regular expression to check at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{6,}$"
        val passwordMatcher = Regex(passwordPattern)

        return passwordMatcher.matches(password)
    }
}
