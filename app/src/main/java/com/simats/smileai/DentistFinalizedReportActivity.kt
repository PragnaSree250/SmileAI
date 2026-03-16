package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.Report
import com.simats.smileai.network.RetrofitClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistFinalizedReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_finalized_report)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnDownload = findViewById<Button>(R.id.btnDownloadReport)
        val btnDone = findViewById<Button>(R.id.btnDone)

        btnBack.setOnClickListener { finish() }

        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
        val deficiency = intent.getStringExtra("EXTRA_AI_DEFICIENCY") ?: "N/A"
        val reasoning = intent.getStringExtra("EXTRA_AI_REASONING") ?: intent.getStringExtra("EXTRA_AI_REPORT") ?: "No analysis found."
        val risk = intent.getStringExtra("EXTRA_AI_RISK") ?: intent.getStringExtra("EXTRA_RISK") ?: "Low"
        val prognosis = intent.getStringExtra("EXTRA_AI_PROGNOSIS") ?: intent.getStringExtra("EXTRA_PROGNOSIS") ?: "Good"
        val strategy = intent.getStringExtra("EXTRA_AI_PLACEMENT") ?: intent.getStringExtra("EXTRA_STRATEGY") ?: "Standard placement"
        val recommendation = intent.getStringExtra("EXTRA_AI_RECOMMENDATION") ?: ""
        val hyperdontia = intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS") ?: "None"
        val symmetry = intent.getStringExtra("EXTRA_AI_SYMMETRY") ?: "Optimal"
        val goldenRatio = intent.getStringExtra("EXTRA_AI_GOLDEN_RATIO") ?: "1.618 Match"
        val missingTeeth = intent.getStringExtra("EXTRA_MISSING_TEETH_STATUS") ?: "None"

        val tvFinalGoldenRatio = findViewById<TextView>(R.id.tvFinalGoldenRatio)
        val tvMedications = findViewById<TextView>(R.id.tvMedications)
        val tvCareInstructions = findViewById<TextView>(R.id.tvCareInstructions)

        // Set report text fields
        findViewById<TextView>(R.id.tvDeficiencyAddressed).text = deficiency
        findViewById<TextView>(R.id.tvReasoning).text = reasoning
        findViewById<TextView>(R.id.tvRiskAnalysis).text = risk
        findViewById<TextView>(R.id.tvAestheticPrognosis).text = prognosis
        // Removed reference to tvPlacementStrategy as it's not in the layout
        findViewById<TextView>(R.id.tvFinalRecommendation).text = recommendation
        findViewById<TextView>(R.id.tvFinalHyperdontia).text = hyperdontia
        findViewById<TextView>(R.id.tvFinalSymmetry).text = symmetry
        tvFinalGoldenRatio.text = goldenRatio

        // Pre-populate meds/tips from extras if available (could be suggested by AI in previous steps)
        val meds = intent.getStringExtra("EXTRA_MEDICATIONS") ?: "Standard Oral Care pack"
        val tips = intent.getStringExtra("EXTRA_CARE_TIPS") ?: "Maintain standard 2x daily brushing and flossing."
        tvMedications.text = meds
        tvCareInstructions.text = tips

        btnDownload.setOnClickListener {
            if (caseId != -1) {
                val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                val token = sharedPref.getString("access_token", "") ?: ""
                val url = "${RetrofitClient.BASE_URL}cases/$caseId/download/report/pdf"
                com.simats.smileai.utils.FileDownloader.downloadFile(this, url, "Report_Case_$caseId.pdf", token)
            } else {
                Toast.makeText(this, "Error: Invalid Case ID", Toast.LENGTH_SHORT).show()
            }
        }

        btnDone.setOnClickListener {
            saveReportAndFinish(caseId, deficiency, reasoning, risk, prognosis, strategy, recommendation, hyperdontia, symmetry, goldenRatio, missingTeeth, meds, tips)
        }
    }

    private fun saveReportAndFinish(
        caseId: Int,
        deficiency: String,
        reasoning: String,
        risk: String,
        prognosis: String,
        strategy: String,
        recommendation: String,
        hyperdontia: String,
        symmetry: String,
        goldenRatio: String,
        missingTeeth: String,
        meds: String,
        tips: String
    ) {
        if (caseId == -1) {
            Toast.makeText(this, "Error: Invalid Case ID", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        val report = Report(
            case_id = caseId,
            deficiency_addressed = deficiency,
            ai_reasoning = reasoning,
            risk_analysis = risk,
            aesthetic_prognosis = prognosis,
            placement_strategy = strategy,
            final_recommendation = recommendation,
            hyperdontia_status = hyperdontia,
            aesthetic_symmetry = symmetry,
            golden_ratio = goldenRatio,
            missing_teeth_status = missingTeeth,
            medications = meds,
            care_instructions = tips
        )

        RetrofitClient.instance.createReport(report).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DentistFinalizedReportActivity, "Report Saved!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@DentistFinalizedReportActivity, DentistExportFilesActivity::class.java)
                    intent.putExtra("EXTRA_CASE_ID", caseId)
                    intent.putExtra("EXTRA_AUTO_DOWNLOAD", true)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = try {
                        val jObjError = JSONObject(response.errorBody()?.string() ?: "{}")
                        jObjError.getString("message")
                    } catch (e: Exception) {
                        response.message() ?: "Unknown Error"
                    }
                    Toast.makeText(this@DentistFinalizedReportActivity, "Failed to save: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistFinalizedReportActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
