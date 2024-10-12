package com.example.personfinanceapp.activity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_details) // Set your layout here

        emailInput = findViewById(R.id.forgot_email)
        sendVerificationButton = findViewById(R.id.send_verification_code)

        sendVerificationButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isNotEmpty()) {
                recoverUsername(email) // Call your recovery function
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recoverUsername(email: String) {
        val recoveryRequest = UsernameRecoveryRequest(email)
        val call = RetrofitClient.instance.recoverUsername(recoveryRequest)

        call.enqueue(object : Callback<Void> { // Adjust the response type based on your API
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotDetailsActivity, "Check your email for your username", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ForgotDetailsActivity, "Recovery failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ForgotDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
