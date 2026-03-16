package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class PatientPasswordChangeSuccessfulActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_password_change_successful)

        // Header Navigation or Back Button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            navigateBack()
        }

        findViewById<TextView>(R.id.btnBackToSettings).setOnClickListener {
            navigateBack()
        }
    }

    private fun navigateBack() {
        // Clear the change password activity from stack and go back to Privacy
        val intent = Intent(this, PatientPrivacyAndSecurityActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
