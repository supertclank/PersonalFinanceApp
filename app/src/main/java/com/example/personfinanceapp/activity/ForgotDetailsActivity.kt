package com.example.personfinanceapp.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.personfinanceapp.R

class ForgotDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_details)

        val backButton = findViewById<Button>(R.id.back)

        // Set an OnClickListener on the back button
        backButton.setOnClickListener {
            // Finish the current activity to go back to the previous one
            finish()
        }
    }
}
