package com.simats.smileai

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity

class PatientTermsAndConditionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_terms_and_conditions)

        // Header Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
