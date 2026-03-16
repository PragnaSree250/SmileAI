package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class DentistApplyAndPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_apply_and_preview)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnFinalizeReport = findViewById<Button>(R.id.btnFinalizeReport)

        // Populate Clinical Summary
        findViewById<android.widget.TextView>(R.id.tvClinicalPatientName).text = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "N/A"
        findViewById<android.widget.TextView>(R.id.tvClinicalTeethCount).text = intent.getStringExtra("EXTRA_TOOTH_NUMBERS") ?: intent.getStringExtra("EXTRA_TOOTH") ?: "0"
        
        val material = intent.getStringExtra("EXTRA_MATERIAL") ?: intent.getStringExtra("EXTRA_AI_SUGGESTED_MATERIAL") ?: "N/A"
        findViewById<android.widget.TextView>(R.id.tvClinicalMaterial).text = material

        btnBack.setOnClickListener {
            finish()
        }

        btnFinalizeReport.setOnClickListener {
            val nextIntent = Intent(this, DentistReport1Activity::class.java).apply {
                putExtras(intent)
                putExtra("IS_FINAL_REPORT", true)
            }
            startActivity(nextIntent)
        }
    }
}
