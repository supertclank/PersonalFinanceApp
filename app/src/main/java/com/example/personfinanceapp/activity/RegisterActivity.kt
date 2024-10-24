package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.UserCreate
import api.data_class.UserRead
import com.example.personfinanceapp.R
import org.mindrot.jbcrypt.BCrypt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Initialize UI components
        val usernameField = findViewById<EditText>(R.id.username)
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val confirmPasswordField = findViewById<EditText>(R.id.confirm_password)
        val firstNameField = findViewById<EditText>(R.id.first_name)
        val lastNameField = findViewById<EditText>(R.id.last_name)
        val phoneNumberField = findViewById<EditText>(R.id.phone_number)
        val registerButton = findViewById<Button>(R.id.register)
        val backButton = findViewById<Button>(R.id.back)

        backButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val firstName = firstNameField.text.toString()
            val lastName = lastNameField.text.toString()
            val phoneNumber = phoneNumberField.text.toString()

            if (validateInputs(
                    username,
                    email,
                    password,
                    confirmPassword,
                    firstName,
                    lastName,
                    phoneNumber
                )
            ) {
                // Check if email is already registered
                checkEmailAndRegister(username, email, password, firstName, lastName, phoneNumber)
            }
        }
    }

    private fun checkEmailAndRegister(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
    ) {
        RetrofitClient.instance.checkUserExistsByEmail(email).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful && response.body() == true) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Email already registered",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Proceed with registration if email is not registered
                    registerUser(username, email, password, firstName, lastName, phoneNumber)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun registerUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
    ) {
        val hashedPassword = hashPassword(password)

        val userCreate =
            UserCreate(username, email, hashedPassword, firstName, lastName, phoneNumber)

        RetrofitClient.instance.createUser(userCreate).enqueue(object : Callback<UserRead> {
            override fun onResponse(call: Call<UserRead>, response: Response<UserRead>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserRead>, t: Throwable) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
    ): Boolean {
        // Validation logic here...
        return true
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}
