package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientProfileActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientId: TextView
    private lateinit var tvPlanType: TextView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvEmailAddress: TextView
    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        // Initialize Views
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientId = findViewById(R.id.tvPatientId)
        tvPlanType = findViewById(R.id.tvPlanType)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber)
        tvEmailAddress = findViewById(R.id.tvEmailAddress)

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
             startActivity(Intent(this, PatientReportActivity::class.java))
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

        findViewById<LinearLayout>(R.id.btnHelp).setOnClickListener {
            startActivity(Intent(this, PatientHelpAndSupportActivity::class.java))
        }

        findViewById<ImageView>(R.id.btnLogoutTop).setOnClickListener {
            val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Retrieve Token
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
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
                        tvPatientId.text = "Patient ID: ${it.patient_id ?: "#PT-" + it.id}"
                        tvPlanType.text = "${it.plan_type ?: "Free"} Plan"
                        tvAvatarInitial.text = it.full_name.take(1).uppercase()
                        tvPhoneNumber.text = it.phone ?: "Not Provided"
                        tvEmailAddress.text = it.email
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@PatientProfileActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                        sharedPref.edit().clear().apply()
                        val intent = Intent(this@PatientProfileActivity, RoleSelectionActivity::class.java)
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
}
