package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class VerifyCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val tvResend = findViewById<TextView>(R.id.tvResend)
        val etCode = findViewById<EditText>(R.id.etCode)

        // Get email passed from CheckEmail screen
        val email = intent.getStringExtra("email") ?: ""
        Log.d("VERIFY_DEBUG", "Email received: $email")

        btnBack.setOnClickListener {
            finish()
        }

        btnVerify.setOnClickListener {
            val code = etCode.text.toString().trim()
            Log.d("VERIFY_DEBUG", "Verify button clicked. Code entered: $code")

            if (code.length == 6) {
                // SUCCESS: Navigate to Dentist Set New Password screen
                Log.d("VERIFY_DEBUG", "Navigating to DentistSetNewPasswordActivity")
                val intent = Intent(this, DentistSetNewPasswordActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("otp", code)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter the 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        tvResend.setOnClickListener {
            Log.d("VERIFY_DEBUG", "Resend clicked. Navigating back to CheckEmailActivity")
            val intent = Intent(this, CheckEmailActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
            finish()
        }
    }
}
