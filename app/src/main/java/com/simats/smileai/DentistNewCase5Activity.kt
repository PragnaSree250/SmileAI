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
import androidx.activity.ComponentActivity
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

class DentistNewCase5Activity : ComponentActivity() {

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
            shade = intent.getStringExtra("EXTRA_SHADE")
        )

        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Creating case and running analysis...")
            setCancelable(false)
            show()
        }

        RetrofitClient.instance.createCase(caseData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()
                    val caseId = body?.case_id ?: -1
                    val suggestedRestoration = body?.suggested_restoration
                    val suggestedMaterial = body?.suggested_material
                    
                    uploadPhotosAndRunAI(caseId, progressDialog, suggestedRestoration, suggestedMaterial)
                } else {
                    progressDialog.dismiss()
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    Toast.makeText(this@DentistNewCase5Activity, "Submission Failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@DentistNewCase5Activity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                if (response.isSuccessful && response.body()?.status == "success") {
                    val result = response.body()!!
                    navigateToReport(result, caseId, suggestedRestoration, suggestedMaterial)
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    Toast.makeText(this@DentistNewCase5Activity, "AI Analysis Error: $err", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@DentistNewCase5Activity, "Network Error during AI: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToReport(result: ApiResponse, realCaseId: Int, suggestedRestoration: String? = null, suggestedMaterial: String? = null) {
        val nextIntent = Intent(this, DentistReport1Activity::class.java).apply {
            putExtras(intent)
            putExtra("EXTRA_CASE_ID", realCaseId)
            putExtra("EXTRA_AI_DEFICIENCY", result.ai_deficiency ?: "General Assessment")
            putExtra("EXTRA_AI_REPORT", result.ai_report ?: "Analysis complete.")
            putExtra("EXTRA_AI_SCORE", result.ai_score ?: 90)
            putExtra("EXTRA_AI_GRADE", result.ai_grade ?: "A")
            putExtra("EXTRA_AI_RECOMMENDATION", result.ai_recommendation ?: "Follow standard care.")
            
            putExtra("EXTRA_AI_SUGGESTED_RESTORATION", suggestedRestoration ?: result.suggested_restoration)
            putExtra("EXTRA_AI_SUGGESTED_MATERIAL", suggestedMaterial ?: result.suggested_material)
            
            putExtra("EXTRA_CARIES_STATUS", result.caries_status)
            putExtra("EXTRA_GUM_STATUS", result.gum_inflammation_status)
            putExtra("EXTRA_AI_SYMMETRY", result.aesthetic_symmetry)
        }
        startActivity(nextIntent)
        finish()
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
