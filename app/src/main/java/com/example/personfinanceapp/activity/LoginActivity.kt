package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.UserRead
import com.example.personfinanceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var connectedCheckBox: CheckBox // Declare the CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Find the Register button by its ID
        val registerButton = findViewById<Button>(R.id.register)

        // Set an OnClickListener on the Register button
        registerButton.setOnClickListener {
            // Start the RegisterActivity
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Find the Forgot button by its ID
        val forgotButton = findViewById<Button>(R.id.forgot)
        connectedCheckBox = findViewById(R.id.api_connected) // Initialize the CheckBox
        val testButton = findViewById<Button>(R.id.test)

        // Set an OnClickListener on the Forgot button
        forgotButton.setOnClickListener {
            // Start the ForgotDetailsActivity.kt
            val intent = Intent(this@LoginActivity, ForgotDetailsActivity::class.java)
            startActivity(intent)


            connectedCheckBox = findViewById(R.id.api_connected) // Initialize the CheckBox
            val testButton = findViewById<Button>(R.id.test)

            testButton.setOnClickListener {
                testApiConnection() // No need to check if the checkbox is checked
            }
        }
        private fun testApiConnection() {
            val call = RetrofitClient.instance.getUsers(0, 1) // Example API call
            call.enqueue(object :
                Callback<List<UserRead>> { // Replace UserRead with the actual response type
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
                        connectedCheckBox.isChecked = true // Set checkbox to true on success
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "API connection failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        connectedCheckBox.isChecked = false // Set checkbox to false on failure
                    }
                }

                override fun onFailure(call: Call<List<UserRead>>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "API connection failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    connectedCheckBox.isChecked = false // Set checkbox to false on failure
                }
            })
        }
    }
}