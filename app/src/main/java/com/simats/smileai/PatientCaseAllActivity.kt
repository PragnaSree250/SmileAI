package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.content.ContextCompat

class PatientCaseAllActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_case_all)

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
            // Already here
        }

        navReports.setOnClickListener {
            // Already here (Reports now points to this case list)
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

         // Tab Logic (Mock)
        val tabAll = findViewById<TextView>(R.id.tabAll)
        val tabActive = findViewById<TextView>(R.id.tabActive)
        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)

        val tabs = listOf(tabAll, tabActive, tabCompleted)
        
        tabs.forEach { tab ->
            tab.setOnClickListener {
               if (tab.id == R.id.tabActive) {
                   startActivity(Intent(this, PatientCaseActiveActivity::class.java))
                   overridePendingTransition(0, 0)
                   finish()
               }
               if (tab.id == R.id.tabCompleted) {
                   startActivity(Intent(this, PatientCaseCompleteActivity::class.java))
                   overridePendingTransition(0, 0)
                   finish()
               }
            }
        }

        fetchCases()
    }

    private fun fetchCases() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""

        if (accessToken.isEmpty() || patientId.isEmpty()) return

        com.simats.smileai.network.RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                if (response.isSuccessful) {
                    val cases = response.body() ?: emptyList()
                    findViewById<TextView>(R.id.tabAll)?.text = "All (${cases.size})"
                    updateCasesUi(cases)
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    android.widget.Toast.makeText(this@PatientCaseAllActivity, "Failed to load: $error", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {
                android.widget.Toast.makeText(this@PatientCaseAllActivity, "Network Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateCasesUi(cases: List<com.simats.smileai.network.Case>) {
        val casesContainer = findViewById<LinearLayout>(R.id.casesContainer)
        casesContainer.removeAllViews()
        val inflater = android.view.LayoutInflater.from(this)

        for (case in cases) {
            val itemView = inflater.inflate(R.layout.item_patient_case, casesContainer, false)

            val tvCaseId = itemView.findViewById<TextView>(R.id.tvCaseId)
            val tvRestorationType = itemView.findViewById<TextView>(R.id.tvRestorationType)
            val tvDentistName = itemView.findViewById<TextView>(R.id.tvDentistName)
            val tvCaseStatus = itemView.findViewById<TextView>(R.id.tvCaseStatus)
            val pbCaseProgress = itemView.findViewById<android.widget.ProgressBar>(R.id.pbCaseProgress)
            val tvProgressText = itemView.findViewById<TextView>(R.id.tvProgressText)
            val tvLastUpdate = itemView.findViewById<TextView>(R.id.tvLastUpdate)

            tvCaseId.text = if (case.id != null) "CASE-${case.id}" else "CASE-XXXX"
            tvRestorationType.text = case.restoration_type ?: "Case Update"
            tvDentistName.text = case.dentist_name ?: "Unknown Dentist"
            tvCaseStatus.text = case.status ?: "Active"
            tvLastUpdate.text = "Last update: " + (case.created_at?.take(10) ?: "recent")

            var progress = 0
            when (case.status?.lowercase()) {
                "completed", "done" -> {
                    progress = 100
                    tvCaseStatus.setBackgroundResource(R.drawable.bg_badge_green)
                    tvCaseStatus.setTextColor(android.graphics.Color.parseColor("#15803d"))
                    pbCaseProgress.progressDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_progress_bar_green)
                }
                "pending analysis" -> {
                    progress = 15
                    tvCaseStatus.setBackgroundResource(R.drawable.bg_badge_orange)
                    tvCaseStatus.setTextColor(android.graphics.Color.parseColor("#c2410c"))
                    pbCaseProgress.progressDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_progress_bar_orange)
                }
                else -> {
                    progress = 65
                    tvCaseStatus.setBackgroundResource(R.drawable.bg_badge_blue)
                    tvCaseStatus.setTextColor(android.graphics.Color.parseColor("#1d4ed8"))
                    pbCaseProgress.progressDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_progress_bar_blue)
                }
            }
            pbCaseProgress.progress = progress
            tvProgressText.text = "$progress%"

            itemView.setOnClickListener {
                val dest = intent.getStringExtra("EXTRA_DESTINATION")
                val target = if (dest == "timeline") {
                    PatientRecentActivity::class.java
                } else {
                    PatientReportActivity::class.java
                }
                val nextIntent = Intent(this, target)
                nextIntent.putExtra("EXTRA_CASE_ID", case.id ?: -1)
                startActivity(nextIntent)
            }

            casesContainer.addView(itemView)
        }
    }
}
