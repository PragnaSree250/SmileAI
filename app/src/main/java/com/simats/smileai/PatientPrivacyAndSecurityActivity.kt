package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity

class PatientPrivacyAndSecurityActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_privacy_and_security)

        // Header Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Change Password Logic
        findViewById<LinearLayout>(R.id.btnChangePassword).setOnClickListener {
            startActivity(Intent(this, PatientChangePasswordActivity::class.java))
        }
    }
}
