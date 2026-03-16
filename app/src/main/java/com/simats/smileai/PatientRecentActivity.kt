package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat

class PatientRecentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_recent)

        // Header Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            // Since this is likely accessed from Cases list, finish returns there
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
             // Already in a case context, maybe go back to Case List?
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        navReports.setOnClickListener {
             startActivity(Intent(this, PatientReportActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        navProfile.setOnClickListener {
            // Navigate to Profile
        }

        // Action Buttons
        findViewById<LinearLayoutCompat>(R.id.btnViewAiAnalysis).setOnClickListener {
            startActivity(Intent(this, PatientViewAiAnalysisActivity::class.java))
        }

        // val btnRecommendation = ...
        findViewById<LinearLayoutCompat>(R.id.btnViewRecommendation).setOnClickListener {
            startActivity(Intent(this, PatientAiRecommendationActivity::class.java))
        }
    }
}
