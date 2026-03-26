package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.smileai.network.Case
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistCasesOverallReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_cases_overall_report)

        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnReport = findViewById<LinearLayout>(R.id.btnReport)
        val btnExport = findViewById<LinearLayout>(R.id.btnExport)

        btnBack.setOnClickListener { finish() }
        
        btnExport.setOnClickListener {
            val intent = Intent(this, DentistExportFilesActivity::class.java)
            intent.putExtra("EXTRA_CASE_ID", caseId)
            startActivity(intent)
        }

        val tvPatientName = findViewById<TextView>(R.id.tvOverallPatientName)
        val tvPatientInfo = findViewById<TextView>(R.id.tvOverallPatientInfo)
        val tvProsthesis = findViewById<TextView>(R.id.tvOverallProsthesis)
        val tvToothNo = findViewById<TextView>(R.id.tvOverallTooth)
        val tvReasoning = findViewById<TextView>(R.id.tvOverallDeficiencyReasoning)

        if (caseId != -1) {
            loadCaseDetails(caseId)
        } else {
            // Fallback to intent extras if case ID is not provided (older navigation logic)
            val name = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "Sarah Johnson"
            val dob = intent.getStringExtra("EXTRA_PATIENT_DOB") ?: "12/05/1989"
            val deficiency = intent.getStringExtra("EXTRA_DEFICIENCY") ?: "General"
            val restoration = intent.getStringExtra("EXTRA_RESTORATION") ?: "Full Crown"
            val tooth = intent.getStringExtra("EXTRA_TOOTH") ?: "14"
            val abutment = intent.getStringExtra("EXTRA_ABUTMENT") ?: "Healthy"
            val gingival = intent.getStringExtra("EXTRA_GINGIVAL") ?: "Symmetrical"

            tvPatientName.text = name
            tvPatientInfo.text = "DOB: $dob"
            tvProsthesis.text = restoration
            tvToothNo.text = "Tooth #$tooth"
            updateReasoning(tvReasoning, deficiency, restoration, tooth, abutment, gingival)
        }

        findViewById<android.widget.Button>(R.id.btnMarkComplete).setOnClickListener {
            if (caseId != -1) {
                markCaseAsComplete(caseId)
            }
        }

    }

    private fun markCaseAsComplete(caseId: Int) {
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""
        if (token.isEmpty()) return

        val statusUpdate = mapOf("status" to "Completed")
        RetrofitClient.instance.updateCaseStatus(caseId, statusUpdate).enqueue(object : Callback<com.simats.smileai.network.ApiResponse> {
            override fun onResponse(call: Call<com.simats.smileai.network.ApiResponse>, response: Response<com.simats.smileai.network.ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DentistCasesOverallReportActivity, "Case marked as Completed!", Toast.LENGTH_SHORT).show()
                    findViewById<android.widget.Button>(R.id.btnMarkComplete).visibility = android.view.View.GONE
                    
                    // Navigate to All Cases
                    val intent = Intent(this@DentistCasesOverallReportActivity, DentistAllCasesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@DentistCasesOverallReportActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<com.simats.smileai.network.ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistCasesOverallReportActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCaseDetails(caseId: Int) {
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""
        if (token.isEmpty()) return

        RetrofitClient.instance.getDentistCases().enqueue(object : Callback<List<Case>> {
            override fun onResponse(call: Call<List<Case>>, response: Response<List<Case>>) {
                if (response.isSuccessful) {
                    val cases = response.body() ?: emptyList()
                    val case = cases.find { it.id == caseId }
                    case?.let {
                        findViewById<TextView>(R.id.tvOverallPatientName).text = "${it.patient_first_name} ${it.patient_last_name}"
                        findViewById<TextView>(R.id.tvOverallPatientInitial).text = it.patient_first_name?.take(1)?.uppercase() ?: "P"
                        findViewById<TextView>(R.id.tvOverallPatientInfo).text = "ID: ${it.patient_id ?: "P-" + it.id} • DOB: ${it.patient_dob} • ${it.patient_gender}"
                        findViewById<TextView>(R.id.tvOverallProsthesis).text = "${it.restoration_type ?: "N/A"} (${it.material ?: "AI Material"})"
                        findViewById<TextView>(R.id.tvOverallTooth).text = "Tooth #${it.tooth_numbers ?: "N/A"}"
                        
                        // New fields
                        findViewById<TextView>(R.id.tvOverallSymmetry).text = it.ai_grade ?: "Optimal (95%)"
                        findViewById<TextView>(R.id.tvOverallGoldenRatio).text = if(it.ai_score != null) "${it.ai_score}% Match" else "High (92%)"
                        findViewById<TextView>(R.id.tvOverallAiGrade).text = it.ai_grade ?: "A+"
                        findViewById<TextView>(R.id.tvOverallRedness).text = if (it.ai_score != null && it.ai_score < 70) "Inflamed" else "Normal"

                        updateReasoning(
                            findViewById(R.id.tvOverallDeficiencyReasoning),
                            it.condition ?: "General",
                            it.restoration_type ?: "Restoration",
                            it.tooth_numbers ?: "affected area",
                            "Healthy", 
                            "Symmetrical" 
                        )
                    }
                }
            }
            override fun onFailure(call: Call<List<Case>>, t: Throwable) {
                Toast.makeText(this@DentistCasesOverallReportActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReportAndPopulate(caseId: Int) {
        RetrofitClient.instance.getReport(caseId).enqueue(object : Callback<com.simats.smileai.network.Report> {
            override fun onResponse(call: Call<com.simats.smileai.network.Report>, response: Response<com.simats.smileai.network.Report>) {
                if (response.isSuccessful) {
                    val report = response.body()
                    report?.let {
                        findViewById<TextView>(R.id.tvOverallMedications).text = it.medications ?: "No medications prescribed."
                        findViewById<TextView>(R.id.tvOverallCareTips).text = it.care_instructions ?: "Standard oral hygiene maintenance suggested."
                    }
                } else {
                    findViewById<TextView>(R.id.tvOverallMedications).text = "No report finalized yet."
                    findViewById<TextView>(R.id.tvOverallCareTips).text = "Finalize the case to see care tips."
                }
            }
            override fun onFailure(call: Call<com.simats.smileai.network.Report>, t: Throwable) {
                Toast.makeText(this@DentistCasesOverallReportActivity, "Failed to fetch report data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateReasoning(tvReasoning: TextView, deficiency: String, restoration: String, tooth: String, abutment: String, gingival: String) {
        tvReasoning.text = when {
            deficiency.contains("Structural", ignoreCase = true) -> 
                "The $restoration proposed plan focuses on structural reinforcement for tooth $tooth. Given the $abutment abutment status, the recommendation provides a durable solution for fracture-compromised areas."
            deficiency.contains("Space", ignoreCase = true) -> 
                "The $restoration successfully restores the edentulous space at $tooth. By accounting for the $gingival gingival architecture, the plan stabilizes the arch and prevents shifting of neighboring teeth."
            deficiency.contains("Aesthetic", ignoreCase = true) -> 
                "The $restoration addresses aesthetic deficiencies at $tooth, aligning with the $gingival gingival profile for optimal facial integration."
            else -> 
                "The AI-recommended $restoration for tooth $tooth addresses the $deficiency deficiency with optimal functional parameters, considering the $abutment abutment health."
        }
    }
}
