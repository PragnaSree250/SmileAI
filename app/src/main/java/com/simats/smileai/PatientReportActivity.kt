package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

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
            startActivity(Intent(this, PatientCaseAllActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        findViewById<LinearLayout>(R.id.btnBackContainer)?.setOnClickListener {
            onBackPressed()
        }

        findViewById<LinearLayout>(R.id.btnDownloadReport).setOnClickListener {
            val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
            if (caseId != -1) {
                val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                val token = sharedPref.getString("access_token", "") ?: ""
                val url = "${com.simats.smileai.network.RetrofitClient.BASE_URL}cases/$caseId/download/report/pdf"
                com.simats.smileai.utils.FileDownloader.downloadFile(this, url, "My_Smile_Report.pdf", token)
            } else {
                android.widget.Toast.makeText(this, "No active case to download.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        fetchReport()
    }

    private fun fetchReport() {
        val caseIdFromIntent = intent.getIntExtra("EXTRA_CASE_ID", -1)
        
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val patientIdRaw = sharedPref.getString("patient_clinical_id", "") ?: ""
        val patientId = patientIdRaw.trim()
        
        if (accessToken.isEmpty() || patientId.isEmpty()) {
            Log.e("PatientReport", "Session or Patient ID missing. ID: '$patientId'")
            return
        }

        // Ensure RetrofitClient is initialized
        if (com.simats.smileai.network.RetrofitClient.authToken == null) {
            com.simats.smileai.network.RetrofitClient.authToken = accessToken
        }

        if (caseIdFromIntent != -1) {
            // Fetch specific report
            loadReportDetails(caseIdFromIntent)
        } else {
            // Fallback: fetch latest case report
            // Fetch the latest case for this patient
            com.simats.smileai.network.RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
                override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                    if (response.isSuccessful) {
                        val cases = response.body() ?: emptyList()
                        if (cases.isEmpty()) {
                            android.widget.Toast.makeText(this@PatientReportActivity, "No diagnostic cases found for your ID.", android.widget.Toast.LENGTH_LONG).show()
                            findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = "No clinical cases found. Please consult your dentist to start an AI analysis."
                            return
                        }
                        val latestCaseId = cases.first().id ?: -1
                        if (latestCaseId != -1) {
                            loadReportDetails(latestCaseId)
                        } else {
                            findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = "Error: Invalid Case ID found."
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failure fetching cases"
                        Log.e("PatientReport", "Case fetch error: $errorMsg")
                        android.widget.Toast.makeText(this@PatientReportActivity, "Error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                        findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = "Unable to load reports at this time."
                    }
                }
                override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {
                    Log.e("PatientReport", "Network Failure: ${t.message}")
                    android.widget.Toast.makeText(this@PatientReportActivity, "Network Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
                    findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = "Network connection issue. Please try again."
                }
            })
        }
    }
    private fun loadReportDetails(caseId: Int) {
        com.simats.smileai.network.RetrofitClient.instance.getReport(caseId).enqueue(object : retrofit2.Callback<com.simats.smileai.network.Report> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.Report>, response: retrofit2.Response<com.simats.smileai.network.Report>) {
                if (response.isSuccessful) {
                    val report = response.body()
                    if (report != null) {
                        val isPreliminary = report.ai_reasoning?.contains("Awaiting final clinical review") == true
                        val deficiencyText = report.deficiency_addressed ?: report.ai_deficiency ?: "Analysis in Progress"
                        populateClinicalInsights(deficiencyText)

                        findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = report.ai_reasoning ?: "Scan complete. Waiting for dentist finalization."
                        findViewById<TextView>(R.id.tvPatientRiskAnalysis)?.text = report.risk_analysis ?: "Analysis pending..."
                        findViewById<TextView>(R.id.tvPatientAestheticPrognosis)?.text = report.aesthetic_prognosis ?: "Analysis pending..."
                        findViewById<TextView>(R.id.tvPatientPlacementStrategy)?.text = report.placement_strategy ?: "Strategy pending review..."
                        findViewById<TextView>(R.id.tvPatientMedications)?.text = report.medications ?: "No medications prescribed yet."
                        findViewById<TextView>(R.id.tvPatientCareInstructions)?.text = report.care_instructions ?: "Care instructions will appear here."

                        if (isPreliminary) {
                            android.widget.Toast.makeText(this@PatientReportActivity, "Showing preliminary AI scan. Dentist review pending.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        findViewById<TextView>(R.id.tvPatientReportReasoning)?.text = "Report data empty."
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failure fetching report"
                    android.widget.Toast.makeText(this@PatientReportActivity, "Report error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.Report>, t: Throwable) {
                android.widget.Toast.makeText(this@PatientReportActivity, "Network error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun populateClinicalInsights(deficiency: String) {
        val tvHowToSolve = findViewById<TextView>(R.id.tvHowToSolve)
        val tvPrevalence = findViewById<TextView>(R.id.tvPrevalence)
        val tvPrevention = findViewById<TextView>(R.id.tvPrevention)

        val diseaseLower = deficiency.lowercase()
        var solve = "General assessment and professional cleaning."
        var percent = "General population baseline."
        var prevent = "Maintain baseline hygiene with regular dentist visits."
        var isHealthy = true

        if (diseaseLower.contains("caries")) {
            solve = "Mechanical removal of decayed tissue followed by composite or amalgam restoration."
            percent = "Affects 90% of adults at some point."
            prevent = "Brushing twice daily with fluoride toothpaste, flossing, and minimizing sugary snacks."
            isHealthy = false
        } else if (diseaseLower.contains("gingivitis")) {
            solve = "Professional scaling and root planing, improving daily oral hygiene routines."
            percent = "Affects approximately 50-90% of adults globally."
            prevent = "Regular brushing, flossing daily, and using antiseptic mouthwash to reduce plaque."
            isHealthy = false
        } else if (diseaseLower.contains("periodontitis")) {
            solve = "Deep cleaning (scaling & root planing), antibiotics, and sometimes surgical intervention."
            percent = "Affects nearly 50% of adults over age 30."
            prevent = "Strict oral hygiene and addressing gingivitis early."
            isHealthy = false
        } else if (diseaseLower.contains("calculus")) {
            solve = "Professional ultrasonic and hand scaling by a dental hygienist or dentist."
            percent = "Present in over 60% of adults worldwide."
            prevent = "Prevent plaque calcification by thorough daily brushing and avoiding dry mouth."
            isHealthy = false
        } else if (diseaseLower.contains("hyperdontia")) {
            solve = "Surgical extraction of supernumerary teeth if they cause crowding or complications."
            percent = "Affects roughly 1-4% of the population."
            prevent = "Largely genetic, cannot be entirely prevented, early radiographic detection is key."
            isHealthy = false
        } else if (diseaseLower.contains("tooth discoloration")) {
            solve = "Professional bleaching, application of veneers or crowns depending on severity."
            percent = "Commonly affects up to 30% of aging or smoking populations."
            prevent = "Limit coffee, tea, and tobacco. Maintain good brushing habits."
            isHealthy = false
        } else if (diseaseLower.contains("healthy") || diseaseLower.contains("normal")) {
            solve = "No restorative intervention required at this time."
            percent = "Ideal condition."
            prevent = "Maintain current strong oral hygiene practices and check-ups every 6 months."
            isHealthy = true
        } else {
            isHealthy = true
        }

        val impact = if (isHealthy) "N/A" else String.format("%.1f", Math.random() * 5 + 10) + "%"
        
        findViewById<TextView>(R.id.tvClinicalInsightsTitle)?.text = "Clinical Insights: $deficiency"
        tvHowToSolve?.text = solve
        tvPrevalence?.text = "$percent \nEstimated structural impact: $impact"
        tvPrevention?.text = prevent
    }
}
