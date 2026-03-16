package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class DentistCaseReportDownloadActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_case_report_download)

        val btnDownloadMore = findViewById<TextView>(R.id.btnDownloadMore)
        val btnDashboard = findViewById<LinearLayout>(R.id.btnDashboard)

        btnDownloadMore.setOnClickListener {
            // Go back to the Export Files screen
            finish()
        }

        btnDashboard.setOnClickListener {
            // Navigate to Dashboard and clear back stack
            val intent = Intent(this, DentistDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
