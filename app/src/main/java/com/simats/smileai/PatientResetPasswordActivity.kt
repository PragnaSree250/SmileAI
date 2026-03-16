package com.simats.smileai

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class PatientResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_reset_password)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnSendReset = findViewById<Button>(R.id.btnSendReset)
        val etEmail = findViewById<EditText>(R.id.etEmail)

        btnBack.setOnClickListener {
            finish()
        }

        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                startActivity(android.content.Intent(this, PatientCheckEmailActivity::class.java))
                finish() 
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
