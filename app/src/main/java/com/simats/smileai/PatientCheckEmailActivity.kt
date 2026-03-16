package com.simats.smileai

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Button
import androidx.activity.ComponentActivity

class PatientCheckEmailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_check_email)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnEnterCode = findViewById<Button>(R.id.btnEnterCode)

        btnBack.setOnClickListener {
            finish()
        }

        btnEnterCode.setOnClickListener {
            // Navigate to Verify Code screen
            startActivity(android.content.Intent(this, PatientVerifyCodeActivity::class.java))
            finish() 
        }
    }
}
