package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.Report
import com.simats.smileai.network.RetrofitClient
import org.json.JSONObject
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistApplyAndPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_apply_and_preview)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnFinalizeReport = findViewById<Button>(R.id.btnFinalizeReport)

        // Populate Clinical Summary
        findViewById<android.widget.TextView>(R.id.tvClinicalPatientName).text = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "N/A"
        findViewById<android.widget.TextView>(R.id.tvClinicalTeethCount).text = intent.getStringExtra("EXTRA_TOOTH_NUMBERS") ?: intent.getStringExtra("EXTRA_TOOTH") ?: "0"
        
        val material = intent.getStringExtra("EXTRA_MATERIAL") ?: intent.getStringExtra("EXTRA_AI_SUGGESTED_MATERIAL") ?: "N/A"
        findViewById<android.widget.TextView>(R.id.tvClinicalMaterial).text = material

        val switchComparison = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchComparison)
        val ivPreviewOriginal = findViewById<android.widget.ImageView>(R.id.ivPreviewOriginal)
        val ivPreviewSimulation = findViewById<android.widget.ImageView>(R.id.ivPreviewSimulation)
        val tvViewLabel = findViewById<android.widget.TextView>(R.id.tvViewLabel)

        switchComparison.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ivPreviewOriginal.alpha = 0.5f
                ivPreviewSimulation.visibility = android.view.View.VISIBLE
                tvViewLabel.text = "AI SIMULATION - ${intent.getStringExtra("EXTRA_AI_RECOMMENDED_SHAPE") ?: "Ovoid"}"
            } else {
                ivPreviewOriginal.alpha = 0.3f
                ivPreviewSimulation.visibility = android.view.View.GONE
                tvViewLabel.text = "INITIAL SCAN"
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnFinalizeReport.setOnClickListener {
            saveReportAndFinish()
        }
    }

    private fun saveReportAndFinish() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""

        // Fix: Ensure global RetrofitClient knows about the token
        RetrofitClient.authToken = token
        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
        
        if (caseId == -1 || token.isEmpty()) {
            Toast.makeText(this, "Error: Case ID or Session missing", Toast.LENGTH_SHORT).show()
            return
        }

        val aiDeficiency = intent.getStringExtra("EXTRA_AI_DEFICIENCY") ?: intent.getStringExtra("EXTRA_DEFICIENCY") ?: "General"
        val aiReport = intent.getStringExtra("EXTRA_AI_REPORT") ?: intent.getStringExtra("EXTRA_AI_REASONING") ?: "Smile parameters are within normal limits."
        val restoration = intent.getStringExtra("EXTRA_RESTORATION") ?: "Restoration"
        val tooth = intent.getStringExtra("EXTRA_TOOTH") ?: "affected area"
        val abutment = intent.getStringExtra("EXTRA_ABUTMENT") ?: "N/A"
        val material = intent.getStringExtra("EXTRA_MATERIAL") ?: intent.getStringExtra("EXTRA_AI_SUGGESTED_MATERIAL") ?: "N/A"
        val gingival = intent.getStringExtra("EXTRA_GINGIVAL") ?: "N/A"

        // Load findings from intent (passed from DentistNewCase5Activity)
        val risk = intent.getStringExtra("EXTRA_AI_RISK") ?: "Low clinical risk identified."
        val prognosis = intent.getStringExtra("EXTRA_AI_PROGNOSIS") ?: "Stable outlook with regular monitoring."
        val strategy = intent.getStringExtra("EXTRA_AI_PLACEMENT") ?: "Standard clinical protocol suggested."

        val medBundle = intent.getBundleExtra("EXTRA_MEDICATION_BUNDLE")
        var medString = "No medications prescribed."
        if (medBundle != null) {
            medString = "${medBundle.getString("name", "Medication")} - ${medBundle.getString("dosage", "")} (${medBundle.getString("frequency", "")})"
        }

        val report = Report(
            case_id = caseId,
            deficiency_addressed = aiDeficiency,
            ai_reasoning = aiReport,
            risk_analysis = risk,
            aesthetic_prognosis = prognosis,
            placement_strategy = strategy,
            final_recommendation = intent.getStringExtra("EXTRA_AI_SUGGESTED_RESTORATION") ?: "$restoration with $material",
            hyperdontia_status = intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS") ?: "None",
            aesthetic_symmetry = intent.getStringExtra("EXTRA_AI_SYMMETRY") ?: "Optimal",
            golden_ratio = intent.getStringExtra("EXTRA_AI_GOLDEN_RATIO") ?: "1.618 Match",
            missing_teeth_status = intent.getStringExtra("EXTRA_MISSING_TEETH_STATUS") ?: "None",
            medications = medString,
            care_instructions = intent.getStringExtra("EXTRA_AI_PLACEMENT") ?: "Standard oral hygiene maintenance suggested."
        )

        Toast.makeText(this, "Finalizing Case...", Toast.LENGTH_SHORT).show()

        if (medBundle != null) {
            val med = com.simats.smileai.network.Medication(
                case_id = caseId,
                name = medBundle.getString("name", ""),
                dosage = medBundle.getString("dosage", ""),
                frequency = medBundle.getString("frequency", ""),
                duration = medBundle.getString("duration", ""),
                notes = medBundle.getString("notes", "")
            )
            RetrofitClient.instance.addMedication(med).enqueue(object : retrofit2.Callback<com.simats.smileai.network.ApiResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, response: retrofit2.Response<com.simats.smileai.network.ApiResponse>) {
                    if (response.isSuccessful) {
                        Log.d("SmileAI", "Medication uploaded successfully")
                    } else {
                        val err = response.errorBody()?.string() ?: response.message()
                        android.widget.Toast.makeText(this@DentistApplyAndPreviewActivity, "Medication Upload Failed: $err", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@DentistApplyAndPreviewActivity, "Medication Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
        }

        RetrofitClient.instance.createReport(report).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DentistApplyAndPreviewActivity, "Case Completed Successfully!", Toast.LENGTH_SHORT).show()
                    val nextIntent = Intent(this@DentistApplyAndPreviewActivity, DentistAllCasesActivity::class.java)
                    nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(nextIntent)
                    finish()
                } else {
                    val errorMsg = try {
                        val jObjError = JSONObject(response.errorBody()?.string() ?: "{}")
                        jObjError.getString("message")
                    } catch (e: Exception) {
                        response.message() ?: "Unknown Error"
                    }
                    Toast.makeText(this@DentistApplyAndPreviewActivity, "Failed to finalize: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistApplyAndPreviewActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
