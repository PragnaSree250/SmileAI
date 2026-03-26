package com.simats.smileai

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.smileai.ml.SmileAIAnalyzer
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.Case
import com.simats.smileai.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistNewCase5Activity : AppCompatActivity() {

    private lateinit var analyzer: SmileAIAnalyzer
    private var faceUriStr: String? = null
    private var intraUriStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_new_case_5)

        analyzer = SmileAIAnalyzer(this)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnPrevious = findViewById<TextView>(R.id.btnPrevious)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnBack.setOnClickListener { finish() }
        btnPrevious.setOnClickListener { finish() }
        
        val tvPatientName = findViewById<TextView>(R.id.tvSummaryPatientName)
        val tvPatientDob = findViewById<TextView>(R.id.tvSummaryPatientDob)
        val tvDeficiency = findViewById<TextView>(R.id.tvSummaryDeficiency)
        val tvTreatment = findViewById<TextView>(R.id.tvSummaryTreatment)
        val tvTooth = findViewById<TextView>(R.id.tvSummaryTooth)
        val tvMaterial = findViewById<TextView>(R.id.tvSummaryMaterial)
        val tvPhotoCount = findViewById<TextView>(R.id.tvSummaryPhotoCount)

        tvPatientName.text = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "Jane Doe"
        tvPatientDob.text = intent.getStringExtra("EXTRA_PATIENT_DOB") ?: "1989-05-12"
        findViewById<TextView>(R.id.tvSummaryGender).text = intent.getStringExtra("EXTRA_GENDER") ?: "Female"
        findViewById<TextView>(R.id.tvSummaryScanId).text = intent.getStringExtra("EXTRA_SCAN_ID") ?: "None"
        
        tvDeficiency.text = intent.getStringExtra("EXTRA_CONDITION") ?: "General Deficiency"
        tvTreatment.text = intent.getStringExtra("EXTRA_RESTORATION") ?: "N/A"
        tvTooth.text = intent.getStringExtra("EXTRA_TOOTH_NUMBERS") ?: "N/A"
        
        val material = intent.getStringExtra("EXTRA_MATERIAL") ?: "N/A"
        val shade = intent.getStringExtra("EXTRA_SHADE") ?: "Not selected"
        tvMaterial.text = "$material (Shade: $shade)"

        faceUriStr = intent.getStringExtra("EXTRA_FACE_IMAGE_URI")
        intraUriStr = intent.getStringExtra("EXTRA_INTRA_IMAGE_URI")
        
        val ivFace = findViewById<ImageView>(R.id.ivSummaryPhotoFace)
        val ivIntra = findViewById<ImageView>(R.id.ivSummaryPhotoIntra)
        
        var photoCount = 0
        if (!faceUriStr.isNullOrEmpty()) {
            ivFace.setImageURI(Uri.parse(faceUriStr))
            ivFace.visibility = View.VISIBLE
            photoCount++
        }
        if (!intraUriStr.isNullOrEmpty()) {
            ivIntra.setImageURI(Uri.parse(intraUriStr))
            ivIntra.visibility = View.VISIBLE
            photoCount++
        }
        tvPhotoCount.text = "$photoCount uploaded"

        btnSubmit.setOnClickListener {
            submitCaseAndRunAI()
        }
    }

    private fun submitCaseAndRunAI() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""
        
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fix: Ensure the global RetrofitClient knows about the token for the interceptor
        RetrofitClient.authToken = token

        val patientFullName = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "Jane Doe"
        val names = patientFullName.split(" ", limit = 2)
        val firstName = names.getOrNull(0) ?: "Jane"
        val lastName = names.getOrNull(1) ?: "Doe"
        
        val caseData = Case(
            patient_first_name = firstName,
            patient_last_name = lastName,
            patient_dob = intent.getStringExtra("EXTRA_PATIENT_DOB"),
            patient_gender = intent.getStringExtra("EXTRA_GENDER"),
            patient_id = intent.getStringExtra("EXTRA_PATIENT_ID"),
            scan_id = intent.getStringExtra("EXTRA_SCAN_ID"),
            tooth_numbers = intent.getStringExtra("EXTRA_TOOTH_NUMBERS"),
            condition = intent.getStringExtra("EXTRA_CONDITION") ?: "General",
            restoration_type = intent.getStringExtra("EXTRA_RESTORATION"),
            material = intent.getStringExtra("EXTRA_MATERIAL"),
            shade = intent.getStringExtra("EXTRA_SHADE"),
            intercanine_width = intent.getStringExtra("EXTRA_INTERCANINE_WIDTH"),
            incisor_length = intent.getStringExtra("EXTRA_INCISOR_LENGTH"),
            abutment_health = intent.getStringExtra("EXTRA_ABUTMENT_HEALTH"),
            gingival_architecture = intent.getStringExtra("EXTRA_GINGIVAL_ARCHITECTURE")
        )

        Log.d("SmileAI", "Submitting Case Context: $caseData")

        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Creating case and running analysis...")
            setCancelable(false)
            show()
        }

        RetrofitClient.instance.createCase(caseData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()
                    Log.d("SmileAI", "Case Creation Success: body=$body")
                    val caseId = body?.case_id ?: -1
                    val suggestedRestoration = body?.suggested_restoration
                    val suggestedMaterial = body?.suggested_material
                    
                    uploadPhotosAndRunAI(caseId, progressDialog, suggestedRestoration, suggestedMaterial)
                } else {
                    progressDialog.dismiss()
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    
                    // Specific message for authorization failure
                    if (response.code() == 401) {
                        Toast.makeText(this@DentistNewCase5Activity, "Session expired or unauthorized. Please login again.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@DentistNewCase5Activity, "Case Creation Failed: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@DentistNewCase5Activity, "Network Error (Case Creation): ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadPhotosAndRunAI(caseId: Int, progressDialog: android.app.ProgressDialog, suggestedRestoration: String? = null, suggestedMaterial: String? = null) {
        val faceFile = faceUriStr?.let { getFileFromUri(Uri.parse(it), "face_photo_upload.jpg") }
        val intraFile = intraUriStr?.let { getFileFromUri(Uri.parse(it), "intra_photo_upload.jpg") }

        if (faceFile != null) {
            progressDialog.setMessage("Uploading face photo...")
            uploadFile(caseId, faceFile, "IMAGE") {
                if (intraFile != null) {
                    progressDialog.setMessage("Uploading intraoral photo...")
                    uploadFile(caseId, intraFile, "IMAGE") {
                        runAIAnalysisOnly(progressDialog, caseId, suggestedRestoration, suggestedMaterial)
                    }
                } else {
                    runAIAnalysisOnly(progressDialog, caseId, suggestedRestoration, suggestedMaterial)
                }
            }
        } else if (intraFile != null) {
            progressDialog.setMessage("Uploading intraoral photo...")
            uploadFile(caseId, intraFile, "IMAGE") {
                runAIAnalysisOnly(progressDialog, caseId, suggestedRestoration, suggestedMaterial)
            }
        } else {
            runAIAnalysisOnly(progressDialog, caseId, suggestedRestoration, suggestedMaterial)
        }
    }

    private fun uploadFile(caseId: Int, file: File, type: String, callback: () -> Unit) {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.instance.uploadCaseFile(caseId, body, typeBody).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d("SmileAI", "File ${file.name} uploaded successfully")
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    Log.e("SmileAI", "File ${file.name} upload failed: $err")
                    if (response.code() == 401) {
                        Toast.makeText(this@DentistNewCase5Activity, "Authorization failed during upload.", Toast.LENGTH_SHORT).show()
                    }
                }
                callback()
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("SmileAI", "File ${file.name} upload error: ${t.message}")
                callback()
            }
        })
    }

    private fun runAIAnalysisOnly(progressDialog: android.app.ProgressDialog, caseId: Int, suggestedRestoration: String? = null, suggestedMaterial: String? = null) {
        progressDialog.setMessage("Finalizing AI analysis on server...")

        RetrofitClient.instance.analyzeCase(caseId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.status == "success") {
                        Log.d("SmileAI", "AI Analysis Success: deficiency=${result.ai_deficiency}, score=${result.ai_score}")
                        navigateToReport(result, caseId, suggestedRestoration, suggestedMaterial)
                    } else {
                        showErrorDialog("Analysis Failed", result.message ?: "The AI engine encountered an error.")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    showErrorDialog("Server Error", "Report generation failed: $err")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                showErrorDialog("Network Error", "Failed to reach server: ${t.message}")
            }
        })
    }

    private fun showErrorDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun navigateToReport(result: ApiResponse, realCaseId: Int, suggestedRestoration: String? = null, suggestedMaterial: String? = null) {
        Log.d("SmileAI", "Navigating to Report for Case ID: $realCaseId")
        try {
            val nextIntent = Intent(this, DentistReport1Activity::class.java).apply {
                // Manually pass critical data to avoid carrying over overly large Intent from previous steps
                putExtra("EXTRA_PATIENT_NAME", intent.getStringExtra("EXTRA_PATIENT_NAME"))
                putExtra("EXTRA_PATIENT_DOB", intent.getStringExtra("EXTRA_PATIENT_DOB"))
                putExtra("EXTRA_GENDER", intent.getStringExtra("EXTRA_GENDER"))
                putExtra("EXTRA_SCAN_ID", intent.getStringExtra("EXTRA_SCAN_ID"))
                putExtra("EXTRA_CONDITION", intent.getStringExtra("EXTRA_CONDITION"))
                putExtra("EXTRA_TOOTH_NUMBERS", intent.getStringExtra("EXTRA_TOOTH_NUMBERS"))
                putExtra("EXTRA_RESTORATION", intent.getStringExtra("EXTRA_RESTORATION"))
                // Pass all results
                putExtra("EXTRA_CASE_ID", realCaseId)
                putExtra("EXTRA_AI_DEFICIENCY", result.ai_deficiency ?: "General Assessment")
                putExtra("EXTRA_AI_REPORT", result.ai_report ?: "Analysis complete.")
                putExtra("EXTRA_AI_SCORE", result.ai_score?.toString() ?: "85")
                putExtra("EXTRA_AI_GRADE", result.ai_grade ?: "B")
                putExtra("EXTRA_AI_RECOMMENDATION", result.ai_recommendation ?: "Standard care.")
                
                // Extra detailed results
                putExtra("EXTRA_CARIES_STATUS", result.caries_status ?: "Normal")
                putExtra("EXTRA_HYPODONTIA_STATUS", result.hypodontia_status ?: "Normal")
                putExtra("EXTRA_DISCOLORATION_STATUS", result.discoloration_status ?: "Normal")
                putExtra("EXTRA_GUM_STATUS", result.gum_inflammation_status ?: "Normal")
                putExtra("EXTRA_CALCULUS_STATUS", result.calculus_status ?: "Normal")
                putExtra("EXTRA_REDNESS_STATUS", result.redness_analysis ?: "Normal")
                putExtra("EXTRA_SYMMETRY_STATUS", result.aesthetic_symmetry ?: "Symmetric")
                putExtra("EXTRA_GOLDEN_RATIO", result.golden_ratio ?: "1.618 Match")
                
                // Clinical data
                putExtra("EXTRA_RISK_ANALYSIS", result.risk_analysis ?: "Stable.")
                putExtra("EXTRA_AESTHETIC_PROGNOSIS", result.aesthetic_prognosis ?: "Good.")
                putExtra("EXTRA_PLACEMENT_STRATEGY", result.placement_strategy ?: "Standard protocol.")
                
                // Original inputs
                putExtra("EXTRA_FACE_IMAGE_URI", intent.getStringExtra("EXTRA_FACE_IMAGE_URI"))
                putExtra("EXTRA_INTRA_IMAGE_URI", intent.getStringExtra("EXTRA_INTRA_IMAGE_URI"))
                putExtra("EXTRA_AI_SUGGESTED_RESTORATION", result.suggested_restoration ?: suggestedRestoration)
                putExtra("EXTRA_AI_SUGGESTED_MATERIAL", result.suggested_material ?: suggestedMaterial)
                
                // Flag to indicate this is a fresh AI report
                putExtra("IS_NEW_ANALYSIS", true)
                // Simplified flags to avoid clearing the entire task stack incorrectly
                // FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP can be problematic if misconfigured
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) 
            }
            
            Log.d("SmileAI", "Starting DentistReport1Activity for Case $realCaseId")
            startActivity(nextIntent)
            Log.d("SmileAI", "Report Activity started successfully")
            finish() // Important: Finish Step 5 so it's not in the stack if user hits back from Report
        } catch (e: Exception) {
            Log.e("SmileAI", "Failed to start Report Activity: ${e.message}")
            Toast.makeText(this, "Error opening report. Please try again from dashboard.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getFileFromUri(uri: Uri, fileName: String): File? {
        return try {
            val file = File(cacheDir, fileName)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e("SmileAI", "Error creating file from URI: ${e.message}")
            null
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        analyzer.close()
    }
}
