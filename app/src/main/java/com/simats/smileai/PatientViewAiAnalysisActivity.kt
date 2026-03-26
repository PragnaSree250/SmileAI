package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.simats.smileai.ml.YoloDetector
import android.graphics.BitmapFactory
import android.net.Uri
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientViewAiAnalysisActivity : AppCompatActivity() {
    private lateinit var detector: YoloDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_view_ai_analysis)

        detector = YoloDetector(this)

        // Ensure RetrofitClient is initialized
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        if (accessToken.isNotEmpty() && RetrofitClient.authToken == null) {
            RetrofitClient.authToken = accessToken
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence)
        val pbConfidence = findViewById<ProgressBar>(R.id.pbConfidence)
        val tvFinding1Title = findViewById<TextView>(R.id.tvFinding1Title)
        val tvFinding1Desc = findViewById<TextView>(R.id.tvFinding1Desc)

        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)

        if (caseId != -1) {
            fetchCaseDetails(caseId)
        } else {
            // Populate with intent data if caseId is missing (fallback)
            populateUiFromIntent()
        }

        btnBack.setOnClickListener { 
            val intent = Intent(this, PatientDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
             startActivity(Intent(this, PatientDashboardActivity::class.java))
             finish()
        }
        findViewById<LinearLayout>(R.id.navCases).setOnClickListener {
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             finish()
        }
        findViewById<LinearLayout>(R.id.navReports).setOnClickListener {
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             finish()
        }
    }

    private fun fetchCaseDetails(caseId: Int) {
        RetrofitClient.instance.getReport(caseId).enqueue(object : Callback<com.simats.smileai.network.Report> {
            override fun onResponse(call: Call<com.simats.smileai.network.Report>, response: Response<com.simats.smileai.network.Report>) {
                if (response.isSuccessful) {
                    val report = response.body() ?: return
                    
                    findViewById<TextView>(R.id.tvFinding1Title).text = report.deficiency_addressed
                    findViewById<TextView>(R.id.tvFinding1Desc).text = report.ai_reasoning
                    
                    val confidence = report.ai_score ?: 85
                    findViewById<TextView>(R.id.tvConfidence).text = "$confidence%"
                    findViewById<ProgressBar>(R.id.pbConfidence).progress = confidence

                    findViewById<TextView>(R.id.tvPatientRecommendation).text = report.final_recommendation ?: "Clinical assessment recommended."
                    findViewById<TextView>(R.id.tvPatientExplanation).text = report.ai_reasoning ?: "Based on AI analysis of your dental scans."
                    
                    findViewById<TextView>(R.id.tvRiskAnalysis).text = report.risk_analysis ?: "Standard clinical risk profile."
                    findViewById<TextView>(R.id.tvAestheticPrognosis).text = report.aesthetic_prognosis ?: "Stable with regular monitoring."
                    findViewById<TextView>(R.id.tvPlacementStrategy).text = report.placement_strategy ?: "Standard clinical protocol."
                    
                    findViewById<TextView>(R.id.tvPatientSymmetry).text = report.aesthetic_symmetry ?: "Optimal"
                    findViewById<TextView>(R.id.tvPatientGoldenRatio).text = report.golden_ratio ?: "1.618"
                    findViewById<TextView>(R.id.tvPatientMedications).text = report.medications ?: "Standard oral care suggested."
                    findViewById<TextView>(R.id.tvPatientCareTips).text = report.care_instructions ?: "Maintain standard 2x daily brushing."
                }
            }
            override fun onFailure(call: Call<com.simats.smileai.network.Report>, t: Throwable) {}
        })
    }

    private fun populateUiFromIntent() {
        val diagnosis = intent.getStringExtra("EXTRA_RECOMMENDATION") ?: "Clinical Assessment"
        findViewById<TextView>(R.id.tvFinding1Title).text = diagnosis
        findViewById<TextView>(R.id.tvFinding1Desc).text = intent.getStringExtra("EXTRA_EXPLANATION") ?: "Awaiting detailed analysis."
        
        val confidence = intent.getIntExtra("EXTRA_CONFIDENCE", 85)
        findViewById<TextView>(R.id.tvConfidence).text = "$confidence%"
        findViewById<ProgressBar>(R.id.pbConfidence).progress = confidence

        findViewById<TextView>(R.id.tvPatientRecommendation).text = diagnosis
        findViewById<TextView>(R.id.tvPatientExplanation).text = intent.getStringExtra("EXTRA_EXPLANATION") ?: "Based on initial AI triage."
        findViewById<TextView>(R.id.tvPatientSymmetry).text = intent.getStringExtra("EXTRA_SYMMETRY") ?: "Optimal"
        findViewById<TextView>(R.id.tvPatientMedications).text = intent.getStringExtra("EXTRA_MEDS") ?: "Standard oral care suggested."
        findViewById<TextView>(R.id.tvPatientCareTips).text = intent.getStringExtra("EXTRA_CARE") ?: "Maintain standard brushing."
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::detector.isInitialized) detector.close()
    }
}
