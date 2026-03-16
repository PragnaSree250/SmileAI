package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import android.widget.TextView

class PatientAiRecommendationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_ai_recommendation)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val textTitle = findViewById<TextView>(R.id.textTitle)
        val textSubtitle = findViewById<TextView>(R.id.textSubtitle)
        val textExplanation = findViewById<TextView>(R.id.textExplanation)

        // Populate with dynamic data
        val restoration = intent.getStringExtra("EXTRA_RESTORATION") ?: "Ceramic Crown"
        val material = intent.getStringExtra("EXTRA_MATERIAL") ?: "Zirconia"

        textTitle.text = restoration
        textSubtitle.text = "$material material"

        textExplanation.text = "Based on the AI analysis of your bite pressure and aesthetic requirements, a $material crown offers the best balance of strength and natural appearance for a molar tooth."

        btnBack.setOnClickListener {
            finish()
        }

        // Bottom Navigation
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navReports = findViewById<LinearLayout>(R.id.navReports)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        navHome.setOnClickListener {
             startActivity(Intent(this, PatientDashboardActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        navCases.setOnClickListener {
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        navReports.setOnClickListener {
             startActivity(Intent(this, PatientReportActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }
    }
}
