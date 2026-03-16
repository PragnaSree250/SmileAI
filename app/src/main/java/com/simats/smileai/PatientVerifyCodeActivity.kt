package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity

class PatientVerifyCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_verify_code)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val etCode = findViewById<EditText>(R.id.etCode)

        // Get email from previous screen
        val email = intent.getStringExtra("email") ?: ""

        btnBack.setOnClickListener {
            finish()
        }

        btnVerify.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.length == 6) {
                // Navigate to Set New Password screen
                val intent = Intent(this, PatientSetNewPasswordActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("otp", code)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
