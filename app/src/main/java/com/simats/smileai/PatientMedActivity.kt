package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PatientMedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_med)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navReports = findViewById<LinearLayout>(R.id.navReports)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        val btnEmergency = findViewById<LinearLayout>(R.id.btnEmergency)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, PatientDashboardActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navCases.setOnClickListener {
            startActivity(Intent(this, PatientCaseAllActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navReports.setOnClickListener {
            startActivity(Intent(this, PatientReportActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnEmergency.setOnClickListener {
            // Handle emergency contact click
        }

        fetchMedications()
    }

    private fun fetchMedications() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""

        if (accessToken.isEmpty() || patientId.isEmpty()) {
            android.widget.Toast.makeText(this, "Session expired or ID missing", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Fetch case-specific medications
        com.simats.smileai.network.RetrofitClient.instance.getPatientMedications(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Medication>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Medication>>, response: retrofit2.Response<List<com.simats.smileai.network.Medication>>) {
                if (response.isSuccessful) {
                    val medications = response.body() ?: emptyList()
                    if (medications.isNotEmpty()) {
                        val med = medications.first()
                        findViewById<android.widget.TextView>(R.id.tvMedName).text = med.name
                        findViewById<android.widget.TextView>(R.id.tvMedFrequency).text = med.frequency
                        findViewById<android.widget.TextView>(R.id.tvMedDuration).text = med.duration
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Medication>>, t: Throwable) {}
        })

        // 2. Fetch report-based suggestions (meds and care tips)
        com.simats.smileai.network.RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                val case = response.body()?.firstOrNull()
                if (case != null) {
                    com.simats.smileai.network.RetrofitClient.instance.getReport(case.id ?: -1).enqueue(object : retrofit2.Callback<com.simats.smileai.network.Report> {
                        override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.Report>, response: retrofit2.Response<com.simats.smileai.network.Report>) {
                            if (response.isSuccessful) {
                                val report = response.body()
                                if (report != null) {
                                    // If no specific med record found, use report's meds
                                    if (findViewById<android.widget.TextView>(R.id.tvMedName).text == "No active medications") {
                                        findViewById<android.widget.TextView>(R.id.tvMedName).text = report.medications ?: "Standard oral care"
                                    }
                                    findViewById<android.widget.TextView>(R.id.tvCareInstructions).text = report.care_instructions ?: "Standard oral hygiene maintenance suggested."
                                }
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.Report>, t: Throwable) {}
                    })
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {}
        })
    }

    private fun updateMedicationUi(medications: List<com.simats.smileai.network.Medication>) {
        // Handled in fetchMedications callback for now
    }
}
