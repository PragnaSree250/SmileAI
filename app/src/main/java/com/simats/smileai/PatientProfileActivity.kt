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

    private lateinit var etFirstName: android.widget.EditText
    private lateinit var etLastName: android.widget.EditText
    private lateinit var tvPatientId: TextView
    private lateinit var tvPlanType: TextView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var etPhoneNumber: android.widget.EditText
    private lateinit var tvEmailAddress: TextView
    private var accessToken: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        // Initialize Views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        tvPatientId = findViewById(R.id.tvPatientId)
        tvPlanType = findViewById(R.id.tvPlanType)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        tvEmailAddress = findViewById(R.id.tvEmailAddress)
        
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
                        val nameParts = it.full_name?.split(" ") ?: listOf("", "")
                        etFirstName.setText(nameParts.getOrNull(0) ?: "")
                        etLastName.setText(nameParts.drop(1).joinToString(" "))
                        
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
                        tvAvatarInitial.visibility = View.VISIBLE
                        
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
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        
        // Validation: Name (Alphabets only)
        val nameRegex = Regex("^[a-zA-Z\\s]+$")
        if (!firstName.matches(nameRegex) || !lastName.matches(nameRegex)) {
            Toast.makeText(this, "Invalid name. Only alphabets are allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = "$firstName $lastName"
        val phone = etPhoneNumber.text.toString().trim()
        
        // Validation: Phone (6,7,8,9 and 10 digits)
        val phoneRegex = Regex("^[6-9]\\d{9}$")
        if (!phone.matches(phoneRegex)) {
            Toast.makeText(this, "Invalid phone number. Must start with 6,7,8,9 and be 10 digits.", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mutableMapOf(
            "full_name" to fullName,
            "phone" to phone
        )

        // Try using updateProfile as it's more comprehensive on some backends
        RetrofitClient.instance.updateProfile(updateData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@PatientProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    loadProfileData() // Reload to sync
                } else {
                    // Fallback to updatePatientProfile if updateProfile fails
                    updatePatientProfileFallback(updateData)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                updatePatientProfileFallback(updateData)
            }
        })
    }

    private fun updatePatientProfileFallback(updateData: Map<String, String>) {
        RetrofitClient.instance.updatePatientProfile(updateData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@PatientProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    loadProfileData()
                } else {
                    Toast.makeText(this@PatientProfileActivity, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
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
