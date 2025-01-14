package com.example.personfinanceapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.RetrofitClient
import api.data_class.UsernameRecoveryRequest
import com.example.personfinanceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotDetailsActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var sendVerificationButton: Button
    private lateinit var backToLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_details)

        emailInput = findViewById(R.id.forgot_email)
        sendVerificationButton = findViewById(R.id.send_verification_code)
        backToLoginButton = findViewById(R.id.back_to_login) // Initialize the button

        sendVerificationButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isNotEmpty()) {
                recoverUsername(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        backToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun recoverUsername(email: String) {
        val recoveryRequest = UsernameRecoveryRequest(email)
        val call = RetrofitClient.instance.recoverUsername(recoveryRequest)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ForgotDetailsActivity,
                        "Check your email for your username",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ForgotDetailsActivity,
                        "Recovery failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ForgotDetailsActivity, "Network error", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
