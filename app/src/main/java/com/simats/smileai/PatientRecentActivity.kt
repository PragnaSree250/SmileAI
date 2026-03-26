package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.simats.smileai.network.RetrofitClient

class PatientRecentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_recent)

        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
        
        // Ensure RetrofitClient is initialized
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        if (accessToken.isNotEmpty() && RetrofitClient.authToken == null) {
            RetrofitClient.authToken = accessToken
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.btnViewAiAnalysis).setOnClickListener {
            val intent = Intent(this, PatientViewAiAnalysisActivity::class.java)
            if (caseId != -1) intent.putExtra("EXTRA_CASE_ID", caseId)
            startActivity(intent)
        }

        findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.btnViewRecommendation).setOnClickListener {
            val intent = Intent(this, PatientAiRecommendationActivity::class.java)
            if (caseId != -1) intent.putExtra("EXTRA_CASE_ID", caseId)
            // Mock data or real if available
            intent.putExtra("EXTRA_RESTORATION", "Zirconia Crown")
            intent.putExtra("EXTRA_MATERIAL", "Premium Zirconia")
            startActivity(intent)
        }
        
        if (caseId != -1) {
            findViewById<android.widget.TextView>(R.id.tvHeaderCaseId)?.text = "Case #$caseId"
            fetchTimeline(caseId)
        }
    }

    private fun fetchTimeline(caseId: Int) {
        com.simats.smileai.network.RetrofitClient.instance.getTimeline(caseId).enqueue(object : retrofit2.Callback<com.simats.smileai.network.TimelineResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.TimelineResponse>, response: retrofit2.Response<com.simats.smileai.network.TimelineResponse>) {
                if (response.isSuccessful) {
                    val events = response.body()?.timeline ?: emptyList()
                    updateTimelineUi(events)
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.TimelineResponse>, t: Throwable) {}
        })
    }

    private fun updateTimelineUi(events: List<com.simats.smileai.network.TimelineEvent>) {
        if (events.isEmpty()) return
        
        val hasSubmitted = events.find { it.event_title.contains("Submitted", true) }
        val hasAnalysis = events.find { it.event_title.contains("Analysis", true) || it.event_title.contains("Analyze", true) }
        val hasReport = events.find { it.event_title.contains("Report", true) || it.event_title.contains("Final", true) }
        val hasAppointment = events.find { it.event_title.contains("Appointment", true) || it.event_title.contains("Scheduled", true) }

        hasSubmitted?.let {
            findViewById<ImageView>(R.id.iconStep1)?.setImageResource(R.drawable.ic_check_circle_green)
            findViewById<android.widget.TextView>(R.id.tvStep1Date)?.text = it.event_date
        }
        hasAnalysis?.let {
            findViewById<ImageView>(R.id.iconStep2)?.setImageResource(R.drawable.ic_check_circle_green)
            findViewById<android.widget.TextView>(R.id.tvStep2Date)?.text = it.event_date
        }
        hasReport?.let {
            findViewById<ImageView>(R.id.iconStep3)?.setImageResource(R.drawable.ic_check_circle_green)
            findViewById<android.widget.TextView>(R.id.tvStep3Date)?.text = it.event_date
        }
        hasAppointment?.let {
            findViewById<ImageView>(R.id.iconStep4)?.setImageResource(R.drawable.ic_check_circle_green)
            findViewById<android.widget.TextView>(R.id.tvStep4Date)?.text = it.event_date
        }
    }
}
