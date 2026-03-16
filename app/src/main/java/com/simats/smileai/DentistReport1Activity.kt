package com.simats.smileai

import android.content.Intent
import com.simats.smileai.network.RetrofitClient
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class DentistReport1Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_report_1)

        val btnAdjust = findViewById<LinearLayout>(R.id.btnAdjust)
        val btnAccept = findViewById<TextView>(R.id.btnAccept)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnNotification = findViewById<ImageView>(R.id.btnNotification)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        val tvReportGrade = findViewById<TextView>(R.id.tvReportGrade)
        val tvReportRecommendation = findViewById<TextView>(R.id.tvReportRecommendation)
        val tvMatchConfidence = findViewById<TextView>(R.id.tvMatchConfidence)
        val tvReportDeficiencyTitle = findViewById<TextView>(R.id.tvReportDeficiencyTitle)
        val tvReportDeficiencyReasoning = findViewById<TextView>(R.id.tvReportDeficiencyReasoning)

        // Retrieve AI data from Intent
        val aiDeficiency = intent.getStringExtra("EXTRA_AI_DEFICIENCY") ?: intent.getStringExtra("EXTRA_DEFICIENCY") ?: "General"
        val aiReport = intent.getStringExtra("EXTRA_AI_REPORT") ?: "Smile parameters are within normal limits."
        val aiScore = intent.getIntExtra("EXTRA_AI_SCORE", 0)
        val aiGrade = intent.getStringExtra("EXTRA_AI_GRADE") ?: "A"
        val aiRecommendation = intent.getStringExtra("EXTRA_AI_RECOMMENDATION") ?: "Routine monitoring."

        val restoration = intent.getStringExtra("EXTRA_RESTORATION") ?: "Restoration"
        val tooth = intent.getStringExtra("EXTRA_TOOTH") ?: "affected area"
        val abutment = intent.getStringExtra("EXTRA_ABUTMENT") ?: "N/A"
        val gingival = intent.getStringExtra("EXTRA_GINGIVAL") ?: "N/A"

        tvReportDeficiencyTitle.text = "$aiDeficiency Resolved"
        tvReportDeficiencyReasoning.text = aiReport
        tvReportGrade.text = "Grade $aiGrade"
        tvReportRecommendation.text = aiRecommendation
        tvMatchConfidence.text = "$aiScore%"

        findViewById<TextView>(R.id.tvReportSuggestedRestoration).text = intent.getStringExtra("EXTRA_AI_SUGGESTED_RESTORATION") ?: "N/A"
        findViewById<TextView>(R.id.tvReportSuggestedMaterial).text = intent.getStringExtra("EXTRA_AI_SUGGESTED_MATERIAL") ?: "N/A"

        // Populate Clinical Findings
        findViewById<TextView>(R.id.tvReportHyperdontia).text = intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS") ?: "None"
        findViewById<TextView>(R.id.tvReportSymmetry).text = intent.getStringExtra("EXTRA_AI_SYMMETRY") ?: "Optimal"
        findViewById<TextView>(R.id.tvReportGoldenRatio).text = intent.getStringExtra("EXTRA_AI_GOLDEN_RATIO") ?: "1.618 Match"
        findViewById<TextView>(R.id.tvReportHypodontia).text = intent.getStringExtra("EXTRA_MISSING_TEETH_STATUS") ?: "None"
        findViewById<TextView>(R.id.tvReportDiscoloration).text = intent.getStringExtra("EXTRA_DISCOLORATION_STATUS") ?: "None"
        findViewById<TextView>(R.id.tvReportInflammation).text = intent.getStringExtra("EXTRA_GUM_STATUS") ?: "Healthy"
        findViewById<TextView>(R.id.tvReportCalculus).text = intent.getStringExtra("EXTRA_CALCULUS_STATUS") ?: "Low"
        findViewById<TextView>(R.id.tvReportRednessAnalysis).text = intent.getStringExtra("EXTRA_CARIES_STATUS") ?: "No significant caries detected."
        
        val risk = when {
            abutment.contains("Compromised", ignoreCase = true) || aiReport.contains("compromised", ignoreCase = true) -> "HIGH RISK: Abutment bone levels are low. Immediate $restoration required to avoid tooth loss."
            else -> "LOW RISK: Mechanical load is within tolerable limits for $abutment foundation."
        }

        val prognosis = when {
            aiDeficiency.contains("Aesthetic", ignoreCase = true) -> "EXCELLENT: $restoration will restore axial symmetry and natural translucency using $gingival parameters."
            else -> "STABLE: Visual integration will be prioritized to match adjacent natural tooth contours."
        }

        val placement = when {
            aiDeficiency.contains("Space", ignoreCase = true) -> "Placement optimized for 'Biological Width' preservation and natural papilla support at $tooth."
            else -> "Strategic placement focuses on 'Ferrule Effect' optimization on $tooth for maximum restoration longevity."
        }

        findViewById<TextView>(R.id.tvReportRiskAnalysis).text = risk
        findViewById<TextView>(R.id.tvReportAestheticPrognosis).text = prognosis
        findViewById<TextView>(R.id.tvReportPlacementStrategy).text = placement

        val isFinal = intent.getBooleanExtra("IS_FINAL_REPORT", false)
        if (isFinal) {
            btnAccept.text = "Finish & Save"
            btnAdjust.visibility = View.GONE
            findViewById<TextView>(R.id.tvTitle)?.text = "Final Case Report"
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnNotification.setOnClickListener {
            val intent = Intent(this, DentistNotificationsActivity::class.java)
            startActivity(intent)
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, DentistMenuBarActivity::class.java)
            startActivity(intent)
        }

        btnAdjust.setOnClickListener {
            val intent = Intent(this, DentistReportAdjustManuallyActivity::class.java).apply {
                putExtras(getIntent())
                putExtra("EXTRA_AI_REASONING", tvReportDeficiencyReasoning.text.toString())
                putExtra("EXTRA_AI_RISK", risk)
                putExtra("EXTRA_AI_PROGNOSIS", prognosis)
                putExtra("EXTRA_AI_PLACEMENT", placement)
            }
            startActivity(intent)
        }

        btnAccept.setOnClickListener {
            if (isFinal) {
                saveReportAndFinish(risk, prognosis, placement)
            } else {
                Toast.makeText(this, "Findings Confirmed! Generating Summary...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DentistApplyAndPreviewActivity::class.java).apply {
                    putExtras(getIntent())
                    putExtra("EXTRA_AI_REASONING", tvReportDeficiencyReasoning.text.toString())
                    putExtra("EXTRA_AI_RISK", risk)
                    putExtra("EXTRA_AI_PROGNOSIS", prognosis)
                    putExtra("EXTRA_AI_PLACEMENT", placement)
                }
                startActivity(intent)
            }
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

        val hyperdontia = intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS") ?: "None"
        val symmetry = intent.getStringExtra("EXTRA_AI_SYMMETRY") ?: "Optimal"
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
            hyperdontia_status = hyperdontia,
            aesthetic_symmetry = symmetry,
            golden_ratio = goldenRatio,
            missing_teeth_status = missingTeeth
        )

        Toast.makeText(this, "Saving Final Report...", Toast.LENGTH_SHORT).show()

        val finalToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
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
                    Toast.makeText(this@DentistReport1Activity, "Failed to save final report", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistReport1Activity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
