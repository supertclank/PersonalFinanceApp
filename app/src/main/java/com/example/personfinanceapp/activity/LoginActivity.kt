package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.personfinanceapp.R

class LoginActivity : AppCompatActivity() {
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

        // Set an OnClickListener on the Forgot button
        forgotButton.setOnClickListener {
            // Start the ForgotDetailsActivity.kt
            val intent = Intent(this@LoginActivity, ForgotDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}
