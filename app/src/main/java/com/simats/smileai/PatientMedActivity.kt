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
            startActivity(Intent(this, PatientCaseAllActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnEmergency.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:04426801551") // Clinic number
            startActivity(intent)
        }

        findViewById<android.widget.Button>(R.id.btnSendMessage).setOnClickListener {
            sendEmergencyMessage()
        }

        fetchMedications()
    }

    private fun sendEmergencyMessage() {
        val etMessage = findViewById<android.widget.EditText>(R.id.etEmergencyMessage)
        val message = etMessage.text.toString().trim()
        
        if (message.isEmpty()) {
            android.widget.Toast.makeText(this, "Please enter a message", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""
        
        val messageData = mapOf(
            "patient_id" to patientId,
            "message" to message
        )

        com.simats.smileai.network.RetrofitClient.instance.sendEmergencyMessage(messageData).enqueue(object : retrofit2.Callback<com.simats.smileai.network.ApiResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, response: retrofit2.Response<com.simats.smileai.network.ApiResponse>) {
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(this@PatientMedActivity, "Message sent to your dentist!", android.widget.Toast.LENGTH_LONG).show()
                    etMessage.setText("")
                } else {
                    android.widget.Toast.makeText(this@PatientMedActivity, "Failed to send message", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, t: Throwable) {
                android.widget.Toast.makeText(this@PatientMedActivity, "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchMedications() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""

        if (accessToken.isEmpty() || patientId.isEmpty()) {
            android.widget.Toast.makeText(this, "Session expired or ID missing", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure RetrofitClient is initialized
        if (com.simats.smileai.network.RetrofitClient.authToken == null) {
            com.simats.smileai.network.RetrofitClient.authToken = accessToken
        }

        // 1. Fetch case-specific medications
        com.simats.smileai.network.RetrofitClient.instance.getPatientMedications(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Medication>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Medication>>, response: retrofit2.Response<List<com.simats.smileai.network.Medication>>) {
                if (response.isSuccessful) {
                    val medications = response.body() ?: emptyList()
                    updateMedicationUi(medications)
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Medication>>, t: Throwable) {
                android.widget.Toast.makeText(this@PatientMedActivity, "Meds Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })

        // 2. Fetch report-based suggestions (care tips)
        com.simats.smileai.network.RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                val case = response.body()?.firstOrNull()
                if (case != null) {
                    com.simats.smileai.network.RetrofitClient.instance.getReport(case.id ?: -1).enqueue(object : retrofit2.Callback<com.simats.smileai.network.Report> {
                        override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.Report>, response: retrofit2.Response<com.simats.smileai.network.Report>) {
                            if (response.isSuccessful) {
                                val report = response.body()
                                if (report != null) {
                                    findViewById<android.widget.TextView>(R.id.tvCareInstructions)?.text = report.care_instructions ?: "Standard oral hygiene maintenance suggested."
                                }
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.Report>, t: Throwable) {
                             android.widget.Toast.makeText(this@PatientMedActivity, "Report Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {
                android.widget.Toast.makeText(this@PatientMedActivity, "Cases Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateMedicationUi(medications: List<com.simats.smileai.network.Medication>) {
        val container = findViewById<LinearLayout>(R.id.llMedicationsContainer) ?: return
        val tvNoMeds = findViewById<android.widget.TextView>(R.id.tvNoMedications)
        
        if (medications.isEmpty()) {
            // Try fallback to AI suggestions from report
            fetchReportFallback()
            return
        }
        
        tvNoMeds?.visibility = android.view.View.GONE
        container.removeAllViews()

        for (med in medications) {
            addMedicationCard(container, med.name ?: "Unnamed", med.dosage ?: "As prescribed", med.frequency, med.duration, med.notes)
        }
    }

    private fun fetchReportFallback() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""
        
        com.simats.smileai.network.RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                val case = response.body()?.firstOrNull()
                if (case != null) {
                    com.simats.smileai.network.RetrofitClient.instance.getReport(case.id ?: -1).enqueue(object : retrofit2.Callback<com.simats.smileai.network.Report> {
                        override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.Report>, response: retrofit2.Response<com.simats.smileai.network.Report>) {
                            if (response.isSuccessful) {
                                val report = response.body()
                                val aiMeds = report?.medications ?: report?.ai_recommendation
                                if (!aiMeds.isNullOrEmpty() && aiMeds != "No medications prescribed.") {
                                    val container = findViewById<LinearLayout>(R.id.llMedicationsContainer)
                                    val tvNoMeds = findViewById<android.widget.TextView>(R.id.tvNoMedications)
                                    tvNoMeds?.visibility = android.view.View.GONE
                                    container?.removeAllViews()
                                    
                                    addMedicationCard(container!!, "AI Suggested Care", aiMeds, "As indicated", "Ongoing", "Generated by SmileAI Analysis")
                                } else {
                                    showNoMedsPlaceholder()
                                }
                            } else {
                                showNoMedsPlaceholder()
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.Report>, t: Throwable) {
                            showNoMedsPlaceholder()
                        }
                    })
                } else {
                    showNoMedsPlaceholder()
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {
                showNoMedsPlaceholder()
            }
        })
    }

    private fun showNoMedsPlaceholder() {
        val tvNoMeds = findViewById<android.widget.TextView>(R.id.tvNoMedications)
        tvNoMeds?.text = "No medications prescribed yet.\nWaiting for your dentist to finalize your treatment plan."
        tvNoMeds?.visibility = android.view.View.VISIBLE
    }

    private fun addMedicationCard(container: LinearLayout, name: String, dosage: String, frequency: String?, duration: String?, notes: String?) {
        val medCard = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 16, 0, 16)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(32, 32, 32, 32)
            background = androidx.core.content.ContextCompat.getDrawable(this@PatientMedActivity, R.drawable.bg_card_dark)
        }

        val icon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(120, 120)
            setImageResource(R.drawable.ic_pill)
            setColorFilter(android.graphics.Color.parseColor("#a855f7"))
            setPadding(20, 20, 20, 20)
            background = androidx.core.content.ContextCompat.getDrawable(this@PatientMedActivity, R.drawable.bg_icon_light_purple)
        }

        val textLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 32
            }
            orientation = LinearLayout.VERTICAL
        }

        val tvName = android.widget.TextView(this).apply {
            text = "$name ($dosage)"
            setTextColor(android.graphics.Color.parseColor("#f8fafc"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 8
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tvFreq = android.widget.TextView(this).apply {
            text = "${frequency ?: "As needed"} • ${duration ?: "Until complete"}"
            setTextColor(android.graphics.Color.parseColor("#94a3b8"))
            textSize = 12f
        }

        subLayout.addView(tvFreq)
        textLayout.addView(tvName)
        textLayout.addView(subLayout)
        
        if (!notes.isNullOrEmpty()) {
            val tvNotes = android.widget.TextView(this).apply {
                text = notes
                setTextColor(android.graphics.Color.parseColor("#cbd5e1"))
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }
            textLayout.addView(tvNotes)
        }

        medCard.addView(icon)
        medCard.addView(textLayout)
        container.addView(medCard)
    }
}
