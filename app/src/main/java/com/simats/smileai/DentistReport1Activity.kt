package com.simats.smileai

import android.content.Intent
import com.simats.smileai.network.RetrofitClient
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.util.Log
import com.google.android.material.card.MaterialCardView

class DentistReport1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("SmileAI", "DentistReport1Activity: Starting onCreate")
            setContentView(R.layout.activity_dentist_report_1)
            
            // Critical views with null-safety
            val btnAdjust = findViewById<LinearLayout>(R.id.btnAdjust)
            val btnAccept = findViewById<TextView>(R.id.btnAccept)
            val btnBack = findViewById<LinearLayout>(R.id.btnBack)
            val btnMenu = findViewById<ImageView>(R.id.btnMenu)

            // Extract data from Intent with safe defaults
            val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
            val isFinal = intent.getBooleanExtra("IS_FINAL_REPORT", false)
            val aiDeficiency = intent.getStringExtra("EXTRA_AI_DEFICIENCY") ?: "General Assessment"
            val aiReport = intent.getStringExtra("EXTRA_AI_REPORT") ?: "Analysis complete."
            val aiScoreString = intent.getStringExtra("EXTRA_AI_SCORE") ?: "85"
            val aiGrade = intent.getStringExtra("EXTRA_AI_GRADE") ?: "B"
            val aiRecommendation = intent.getStringExtra("EXTRA_AI_RECOMMENDATION") ?: "Standard care."
            
            val risk = intent.getStringExtra("EXTRA_RISK_ANALYSIS") ?: "Stable clinical situation."
            val prognosis = intent.getStringExtra("EXTRA_AESTHETIC_PROGNOSIS") ?: "Excellent aesthetic outlook."
            val placement = intent.getStringExtra("EXTRA_PLACEMENT_STRATEGY") ?: "Standard placement protocol."
            
            val hypodontiaStatus = intent.getStringExtra("EXTRA_HYPODONTIA_STATUS") ?: "Normal"
            val discolorationStatus = intent.getStringExtra("EXTRA_DISCOLORATION_STATUS") ?: "Normal"
            val gumStatus = intent.getStringExtra("EXTRA_GUM_STATUS") ?: "Healthy"
            val calculusStatus = intent.getStringExtra("EXTRA_CALCULUS_STATUS") ?: "Normal"
            val symmetry = intent.getStringExtra("EXTRA_SYMMETRY_STATUS") ?: "Symmetric"
            val goldenRatio = intent.getStringExtra("EXTRA_GOLDEN_RATIO") ?: "1.618 Match"
            val rednessStatus = intent.getStringExtra("EXTRA_REDNESS_STATUS") ?: "Normal"
            val cariesStatus = intent.getStringExtra("EXTRA_CARIES_STATUS") ?: "Normal"

            Log.d("SmileAI", "DentistReport1Activity: Data extracted for Case $caseId")

            // Populate UI
            findViewById<TextView>(R.id.tvMatchConfidence)?.text = "$aiScoreString%"
            findViewById<TextView>(R.id.tvReportDeficiencyTitle)?.text = if (aiDeficiency.contains("Resolved")) aiDeficiency else "$aiDeficiency Resolved"
            findViewById<TextView>(R.id.tvReportGrade)?.text = "Grade $aiGrade"
            findViewById<TextView>(R.id.tvReportRecommendation)?.text = aiRecommendation
            findViewById<TextView>(R.id.tvReportDeficiencyReasoning)?.text = aiReport
            
            findViewById<TextView>(R.id.tvReportSuggestedRestoration)?.text = intent.getStringExtra("EXTRA_AI_SUGGESTED_RESTORATION") ?: "Bridge"
            findViewById<TextView>(R.id.tvReportSuggestedMaterial)?.text = intent.getStringExtra("EXTRA_AI_SUGGESTED_MATERIAL") ?: "Zirconia"
            
            findViewById<TextView>(R.id.tvReportHypodontia)?.text = hypodontiaStatus
            findViewById<TextView>(R.id.tvReportDiscoloration)?.text = discolorationStatus
            findViewById<TextView>(R.id.tvReportInflammation)?.text = gumStatus
            findViewById<TextView>(R.id.tvReportCalculus)?.text = calculusStatus
            findViewById<TextView>(R.id.tvReportSymmetry)?.text = symmetry
            findViewById<TextView>(R.id.tvReportGoldenRatio)?.text = goldenRatio
            findViewById<TextView>(R.id.tvReportRednessAnalysis)?.text = "Caries: $cariesStatus | Redness: $rednessStatus"
            
            findViewById<TextView>(R.id.tvReportRiskAnalysis)?.text = risk
            findViewById<TextView>(R.id.tvReportAestheticPrognosis)?.text = prognosis
            findViewById<TextView>(R.id.tvReportPlacementStrategy)?.text = placement

            // Setup icons and styling
            updateFindingIcon(findViewById(R.id.ivHypodontiaIcon), hypodontiaStatus)
            updateFindingIcon(findViewById(R.id.ivDiscolorationIcon), discolorationStatus)
            updateFindingIcon(findViewById(R.id.ivGumIcon), gumStatus)
            updateFindingIcon(findViewById(R.id.ivCalculusIcon), calculusStatus)
            updateFindingIcon(findViewById(R.id.ivSymmetryIcon), symmetry)
            
            styleReportCard(findViewById(R.id.cardRiskAnalysis), risk, "risk")
            styleReportCard(findViewById(R.id.cardPrognosis), prognosis, "prognosis")
            styleReportCard(findViewById(R.id.cardStrategy), placement, "strategy")

            // Photo Previews
            val faceUriString = intent.getStringExtra("EXTRA_FACE_IMAGE_URI")
            val intraUriString = intent.getStringExtra("EXTRA_INTRA_IMAGE_URI")
            if (!faceUriString.isNullOrEmpty()) {
                findViewById<ImageView>(R.id.ivSummaryPhotoFace)?.setImageURI(android.net.Uri.parse(faceUriString))
            }
            if (!intraUriString.isNullOrEmpty()) {
                findViewById<ImageView>(R.id.ivSummaryPhotoIntra)?.setImageURI(android.net.Uri.parse(intraUriString))
            }

            if (isFinal) {
                btnAccept?.text = "Finish & Save"
                btnAdjust?.visibility = View.GONE
            }

            // Click Listeners
            btnBack?.setOnClickListener { finish() }
            btnMenu?.setOnClickListener {
                startActivity(Intent(this, DentistMenuBarActivity::class.java))
            }
            btnAdjust?.setOnClickListener {
                val adjustIntent = Intent(this, DentistReportAdjustManuallyActivity::class.java).apply {
                    putExtras(intent)
                }
                startActivity(adjustIntent)
            }
            btnAccept?.setOnClickListener {
                if (isFinal) {
                    saveReportAndFinish(risk, prognosis, placement)
                } else {
                    val previewIntent = Intent(this, DentistApplyAndPreviewActivity::class.java).apply {
                        putExtras(intent)
                    }
                    startActivity(previewIntent)
                }
            }

            // Fetch details if data looks like placeholders
            if (isFinal || risk.contains("Stable clinical situation") || caseId != -1) {
                fetchCaseDetails(caseId)
            }

            Log.d("SmileAI", "DentistReport1Activity: onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("SmileAI", "CRITICAL ERROR in DentistReport1Activity.onCreate: ${e.message}", e)
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Report Startup Error")
                .setMessage("Error initializing report: ${e.message}\n\nPlease check Logcat.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }
    }

    private fun saveReportAndFinish(risk: String, prognosis: String, strategy: String) {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""
        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
        val deficiency = intent.getStringExtra("EXTRA_AI_DEFICIENCY") ?: intent.getStringExtra("EXTRA_DEFICIENCY") ?: "General"
        val reasoning = findViewById<TextView>(R.id.tvReportDeficiencyReasoning).text.toString()
        val restoration = intent.getStringExtra("EXTRA_RESTORATION") ?: "Restoration"
        val material = intent.getStringExtra("EXTRA_MATERIAL") ?: "N/A"

        if (caseId == -1 || token.isEmpty()) {
             Toast.makeText(this, "Error: Case ID or Session missing", Toast.LENGTH_SHORT).show()
             return
        }

        // Fix: Ensure global RetrofitClient knows about the token
        RetrofitClient.authToken = token

        val hyperdontiaStatus = intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS") ?: "None"
        val symmetryStatus = intent.getStringExtra("EXTRA_AI_SYMMETRY") ?: "Optimal"
        val goldenRatio = intent.getStringExtra("EXTRA_AI_GOLDEN_RATIO") ?: "1.618 Match"
        val missingTeeth = intent.getStringExtra("EXTRA_MISSING_TEETH_STATUS") ?: "None"

        val report = com.simats.smileai.network.Report(
            case_id = caseId,
            deficiency_addressed = deficiency,
            ai_reasoning = reasoning,
            risk_analysis = risk,
            aesthetic_prognosis = prognosis,
            placement_strategy = strategy,
            final_recommendation = intent.getStringExtra("EXTRA_AI_SUGGESTED_RESTORATION") ?: "$restoration with $material",
            hyperdontia_status = hyperdontiaStatus,
            aesthetic_symmetry = symmetryStatus,
            golden_ratio = goldenRatio,
            missing_teeth_status = missingTeeth
        )

        Toast.makeText(this, "Saving Final Report...", Toast.LENGTH_SHORT).show()

        com.simats.smileai.network.RetrofitClient.instance.createReport(report).enqueue(object : retrofit2.Callback<com.simats.smileai.network.ApiResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, response: retrofit2.Response<com.simats.smileai.network.ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DentistReport1Activity, "Case Completed Successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@DentistReport1Activity, DentistFinalizedReportActivity::class.java).apply {
                        putExtras(getIntent())
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@DentistReport1Activity, "Failed to save final report: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistReport1Activity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCaseDetails(caseId: Int) {
        if (caseId == -1) return
        
        Log.d("SmileAI", "Fetching details for Case $caseId")
        RetrofitClient.instance.getReport(caseId).enqueue(object : retrofit2.Callback<com.simats.smileai.network.Report> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.Report>, response: retrofit2.Response<com.simats.smileai.network.Report>) {
                if (response.isSuccessful && response.body() != null) {
                    val report = response.body()!!
                    Log.d("SmileAI", "Detailed report fetched successfully for Case $caseId")
                    populateData(report)
                } else if (response.code() == 404) {
                    Log.d("SmileAI", "No finalized report yet for Case $caseId. Using initial AI results.")
                } else {
                    Log.e("SmileAI", "Failed to fetch report details: ${response.code()}")
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.Report>, t: Throwable) {
                Log.e("SmileAI", "Error fetching report details: ${t.message}")
            }
        })
    }

    private fun populateData(report: com.simats.smileai.network.Report) {
        try {
            findViewById<TextView>(R.id.tvMatchConfidence)?.text = "${report.ai_score ?: 85}%"
            findViewById<TextView>(R.id.tvReportGrade)?.text = "Grade ${report.ai_grade ?: "B"}"
            findViewById<TextView>(R.id.tvReportDeficiencyReasoning)?.text = report.ai_reasoning ?: "No reasoning provided."
            findViewById<TextView>(R.id.tvReportRecommendation)?.text = report.ai_recommendation ?: "Standard care."
            
            findViewById<TextView>(R.id.tvReportSuggestedRestoration)?.text = report.suggested_restoration ?: "N/A"
            findViewById<TextView>(R.id.tvReportSuggestedMaterial)?.text = report.suggested_material ?: "N/A"
            
            findViewById<TextView>(R.id.tvReportHypodontia)?.text = report.missing_teeth_status ?: "Normal"
            findViewById<TextView>(R.id.tvReportDiscoloration)?.text = report.discoloration_status ?: "Normal"
            findViewById<TextView>(R.id.tvReportInflammation)?.text = report.gum_inflammation_status ?: "Normal"
            findViewById<TextView>(R.id.tvReportCalculus)?.text = report.calculus_status ?: "Normal"
            findViewById<TextView>(R.id.tvReportSymmetry)?.text = report.aesthetic_symmetry ?: "Symmetric"
            findViewById<TextView>(R.id.tvReportGoldenRatio)?.text = report.golden_ratio ?: "1.618 Match"
            
            findViewById<TextView>(R.id.tvReportRiskAnalysis)?.text = report.risk_analysis ?: "Stable."
            findViewById<TextView>(R.id.tvReportAestheticPrognosis)?.text = report.aesthetic_prognosis ?: "Good."
            findViewById<TextView>(R.id.tvReportPlacementStrategy)?.text = report.placement_strategy ?: "Standard."

            // Update icons
            updateFindingIcon(findViewById(R.id.ivHypodontiaIcon), report.missing_teeth_status ?: "Normal")
            updateFindingIcon(findViewById(R.id.ivDiscolorationIcon), report.discoloration_status ?: "Normal")
            updateFindingIcon(findViewById(R.id.ivGumIcon), report.gum_inflammation_status ?: "Normal")
            updateFindingIcon(findViewById(R.id.ivCalculusIcon), report.calculus_status ?: "Normal")
            updateFindingIcon(findViewById(R.id.ivSymmetryIcon), report.aesthetic_symmetry ?: "Symmetric")

            styleReportCard(findViewById(R.id.cardRiskAnalysis), report.risk_analysis ?: "", "risk")
            styleReportCard(findViewById(R.id.cardPrognosis), report.aesthetic_prognosis ?: "", "prognosis")
            styleReportCard(findViewById(R.id.cardStrategy), report.placement_strategy ?: "", "strategy")
            
        } catch (e: Exception) {
            Log.e("SmileAI", "Error in populateData: ${e.message}")
        }
    }

    private fun styleReportCard(card: MaterialCardView?, text: String, type: String) {
        if (card == null) return
        
        val normalized = text.lowercase()
        when {
            normalized.contains("high risk") || normalized.contains("critical") -> {
                card.strokeColor = Color.parseColor("#ef4444") // Red
                card.strokeWidth = 2
                card.setCardBackgroundColor(Color.parseColor("#2d1a1a"))
            }
            normalized.contains("moderate") || normalized.contains("caution") -> {
                card.strokeColor = Color.parseColor("#f59e0b") // Amber
                card.strokeWidth = 2
                card.setCardBackgroundColor(Color.parseColor("#2d241a"))
            }
            normalized.contains("excellent") || normalized.contains("low risk") || normalized.contains("optimal") -> {
                card.strokeColor = Color.parseColor("#10b981") // Emerald
                card.strokeWidth = 2
                card.setCardBackgroundColor(Color.parseColor("#1a2d24"))
            }
            else -> {
                card.strokeColor = Color.parseColor("#334155")
                card.strokeWidth = 1
                card.setCardBackgroundColor(Color.parseColor("#1e293b"))
            }
        }
    }

    private fun updateFindingIcon(imageView: ImageView?, status: String) {
        if (imageView == null) return
        val normalized = status.lowercase()
        val isOptimal = normalized.contains("none") || 
                        normalized.contains("optimal") || 
                        normalized.contains("healthy") || 
                        normalized.contains("low") ||
                        normalized.contains("minimal") ||
                        normalized.contains("match") ||
                        normalized.contains("normal")
        
        if (isOptimal) {
            imageView.setImageResource(R.drawable.ic_check_circle)
            imageView.setColorFilter(Color.parseColor("#10b981"))
        } else {
            imageView.setImageResource(R.drawable.ic_warning_triangle)
            imageView.setColorFilter(Color.parseColor("#f59e0b"))
        }
    }
}
