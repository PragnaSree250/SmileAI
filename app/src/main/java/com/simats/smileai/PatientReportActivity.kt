package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class PatientReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_report)

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
            // Already here
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        fetchReport()
    }

    private fun fetchReport() {
        val caseIdFromIntent = intent.getIntExtra("EXTRA_CASE_ID", -1)
        
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""
        if (accessToken.isEmpty()) return

        if (caseIdFromIntent != -1) {
            // Fetch specific report
            loadReportDetails(caseIdFromIntent)
        } else {
            // Fallback: fetch latest case report
            if (patientId.isEmpty()) return
            com.simats.smileai.network.RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
                override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                    val case = response.body()?.firstOrNull()
                    if (case != null && case.id != null) {
                        loadReportDetails(case.id)
                    }
                }
                override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {}
            })
        }
    }

    private fun loadReportDetails(caseId: Int) {
        com.simats.smileai.network.RetrofitClient.instance.getReport(caseId).enqueue(object : retrofit2.Callback<com.simats.smileai.network.Report> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.Report>, response: retrofit2.Response<com.simats.smileai.network.Report>) {
                if (response.isSuccessful) {
                    val report = response.body()
                    if (report != null) {
                        findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = report.ai_reasoning
                        findViewById<TextView>(R.id.tvPatientRiskAnalysis)?.text = report.risk_analysis ?: "Low clinical risk identified."
                        findViewById<TextView>(R.id.tvPatientAestheticPrognosis)?.text = report.aesthetic_prognosis ?: "Good aesthetic match predicted."
                        findViewById<TextView>(R.id.tvPatientPlacementStrategy)?.text = report.placement_strategy ?: "Standard placement optimized for comfort."
                        findViewById<TextView>(R.id.tvPatientMedications)?.text = report.medications ?: "No medications prescribed."
                        findViewById<TextView>(R.id.tvPatientCareInstructions)?.text = report.care_instructions ?: "Standard oral hygiene maintenance suggested."
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.Report>, t: Throwable) {}
        })
    }
}
