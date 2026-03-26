package com.simats.smileai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class PatientProfileActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientId: TextView
    private lateinit var tvPlanType: TextView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var etPhoneNumber: android.widget.EditText
    private lateinit var tvEmailAddress: TextView
    private lateinit var ivProfilePhoto: ImageView
    private var accessToken: String = ""
    private var tempImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { uploadPhoto(it) }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { uploadPhoto(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        // Initialize Views
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientId = findViewById(R.id.tvPatientId)
        tvPlanType = findViewById(R.id.tvPlanType)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        tvEmailAddress = findViewById(R.id.tvEmailAddress)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        
        val btnEditPhoto = findViewById<ImageView>(R.id.btnEditPhoto)
        btnEditPhoto.setOnClickListener {
            showImagePickerDialog()
        }
        
        val btnSaveChanges = findViewById<android.widget.Button>(R.id.btnSaveChanges)
        btnSaveChanges.setOnClickListener {
            saveProfileData()
        }

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
             startActivity(Intent(this, PatientDashboardActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        findViewById<LinearLayout>(R.id.navCases).setOnClickListener {
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        findViewById<LinearLayout>(R.id.navReports).setOnClickListener {
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        // Settings Buttons
        findViewById<LinearLayout>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, PatientNotificationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnPrivacy).setOnClickListener {
            startActivity(Intent(this, PatientPrivacyAndSecurityActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnTerms).setOnClickListener {
            startActivity(Intent(this, PatientTermsAndConditionsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnDeleteAccount).setOnClickListener {
            showDeleteAccountConfirmation()
        }


        findViewById<ImageView>(R.id.btnLogoutTop).setOnClickListener {
            val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            RetrofitClient.authToken = null
            val intent = Intent(this, SmartLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Retrieve Token
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SmartLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Ensure RetrofitClient is initialized (Fix for session expired issue)
        if (RetrofitClient.authToken == null) {
            RetrofitClient.authToken = accessToken
        }

        // Load Data
        loadProfileData()
    }

    private fun loadProfileData() {
        RetrofitClient.instance.getPatientProfile().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val user = response.body()?.user
                    user?.let {
                        tvPatientName.text = it.full_name
                        
                        // Format Patient ID: p0011 -> p-0011
                        val rawId = it.patient_id ?: "#PT-${it.id}"
                        val formattedId = if (rawId.startsWith("p", ignoreCase = true) && !rawId.contains("-")) {
                            "p-" + rawId.substring(1)
                        } else {
                            rawId
                        }
                        tvPatientId.text = "Patient ID: $formattedId"
                        
                        tvPlanType.text = "${it.plan_type ?: "Free"} Plan"
                        val initial = it.full_name.trim().take(1).uppercase()
                        tvAvatarInitial.text = initial
                        
                        // Load profile photo if exists
                        it.profile_photo?.let { photoUrl ->
                            tvAvatarInitial.visibility = View.GONE
                            ivProfilePhoto.visibility = View.VISIBLE
                            Glide.with(this@PatientProfileActivity)
                                .load(RetrofitClient.BASE_URL + photoUrl)
                                .circleCrop()
                                .into(ivProfilePhoto)
                        } ?: run {
                            tvAvatarInitial.visibility = View.VISIBLE
                            ivProfilePhoto.visibility = View.GONE
                        }
                        
                        etPhoneNumber.setText(it.phone ?: "")
                        tvEmailAddress.text = it.email
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@PatientProfileActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                        sharedPref.edit().clear().apply()
                        RetrofitClient.authToken = null
                        val intent = Intent(this@PatientProfileActivity, SmartLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to load profile"
                        Toast.makeText(this@PatientProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@PatientProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val phone = etPhoneNumber.text.toString().trim()
        
        val updateData = mutableMapOf(
            "phone" to phone
        )

        RetrofitClient.instance.updatePatientProfile(updateData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@PatientProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PatientProfileActivity, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@PatientProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        val imageFile = File(cacheDir, "temp_profile_image.jpg")
        tempImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
        tempImageUri?.let { takePictureLauncher.launch(it) }
    }

    private fun uploadPhoto(uri: Uri) {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Uploading photo...")
            setCancelable(false)
            show()
        }

        val file = File(cacheDir, "profile_upload.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.instance.uploadProfilePhoto(body).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@PatientProfileActivity, "Photo updated!", Toast.LENGTH_SHORT).show()
                    val photoUrl = response.body()?.photo_url
                    if (photoUrl != null) {
                        tvAvatarInitial.visibility = View.GONE
                        ivProfilePhoto.visibility = View.VISIBLE
                        Glide.with(this@PatientProfileActivity)
                            .load(RetrofitClient.BASE_URL + photoUrl)
                            .circleCrop()
                            .into(ivProfilePhoto)
                    }
                } else {
                    Toast.makeText(this@PatientProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@PatientProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteAccountConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you absolutely sure you want to delete your account? This action is permanent and all your data will be lost.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Deleting account...")
            setCancelable(false)
            show()
        }

        RetrofitClient.instance.deleteProfile().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@PatientProfileActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    
                    // Clear session and logout
                    val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                    sharedPref.edit().clear().apply()
                    RetrofitClient.authToken = null
                    
                    val intent = Intent(this@PatientProfileActivity, SmartLoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to delete account"
                    Toast.makeText(this@PatientProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@PatientProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
