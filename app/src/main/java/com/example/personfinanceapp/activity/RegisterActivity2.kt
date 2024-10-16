package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.ProfileCreate
import api.data_class.ProfileRead
import com.example.personfinanceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_2) // Set the layout for the activity

        // Initialize UI components
        val backButton = findViewById<Button>(R.id.back)
        val firstNameField = findViewById<EditText>(R.id.first_name)
        val lastNameField = findViewById<EditText>(R.id.last_name)
        val phoneNumberField = findViewById<EditText>(R.id.phone_number)
        val registerButton = findViewById<Button>(R.id.register)

        // Set a click listener for the back button to finish the current activity
        backButton.setOnClickListener {
            finish()
        }

        // Set a click listener for the register button
        registerButton.setOnClickListener {
            // Retrieve input values
            val firstName = firstNameField.text.toString()
            val lastName = lastNameField.text.toString()
            val phoneNumber = phoneNumberField.text.toString()

            // Get USER_ID from the intent
            val userId = intent.getStringExtra("USER_ID")?.toInt() // Convert to Int

            // Check if USER_ID is not null and validate inputs
            if (userId != null && validateInputs(firstName, lastName, phoneNumber)) {
                // Disable the register button to prevent multiple submissions
                registerButton.isEnabled = false
                // Create a ProfileCreate object with the input values
                val profileCreate = ProfileCreate(
                    user_id = userId,
                    first_name = firstName,
                    last_name = lastName,
                    phone_number = phoneNumber
                )
                // Make an API call to create the profile
                RetrofitClient.instance.createProfile(profileCreate)
                    .enqueue(object : Callback<ProfileRead> {
                        override fun onResponse(
                            call: Call<ProfileRead>,
                            response: Response<ProfileRead>
                        ) {
                            if (response.isSuccessful) {
                                // Handle successful response and start com.example.personfinanceapp.activity.LoginActivity
                                val intent =
                                    Intent(this@RegisterActivity2, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Log the error message if the response is not successful
                                Log.e(
                                    "API Error",
                                    response.errorBody()?.string() ?: "Unknown error"
                                )
                                Toast.makeText(
                                    this@RegisterActivity2,
                                    "Profile creation failed. Please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                registerButton.isEnabled = true // Re-enable the button
                            }
                        }

                        override fun onFailure(call: Call<ProfileRead>, t: Throwable) {
                            // Log the error if the API call fails
                            Log.e("API Failure", t.message ?: "Unknown failure")
                            Toast.makeText(
                                this@RegisterActivity2,
                                "Network error. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                            registerButton.isEnabled = true // Re-enable the button
                        }
                    })
            }
        }
    }

    // Function to validate inputs before making the API call
    private fun validateInputs(firstName: String, lastName: String, phoneNumber: String): Boolean {
        // Check if first name is empty or contains non-alphabetic characters
        if (firstName.isEmpty() || !firstName.matches(Regex("^[A-Za-z]+$"))) {
            Toast.makeText(
                this,
                "First name is required and must contain only alphabetic characters",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Check if surname is empty or contains non-alphabetic characters
        if (lastName.isEmpty() || !lastName.matches(Regex("^[A-Za-z]+$"))) {
            Toast.makeText(
                this,
                "Surname is required and must contain only alphabetic characters",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Check if phone number is empty or does not match the specified format
        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("^[0-9]{10,15}$"))) {
            Toast.makeText(
                this,
                "Valid phone number is required (10 to 15 digits)",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true // All validations passed
    }
}
