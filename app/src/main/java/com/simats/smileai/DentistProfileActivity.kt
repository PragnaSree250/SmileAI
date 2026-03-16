package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistProfileActivity : ComponentActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etClinicAddress: EditText
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileSpecialization: TextView
    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_profile)

        // Initialize Views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etClinicAddress = findViewById(R.id.etClinicAddress)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileSpecialization = findViewById(R.id.tvProfileSpecialization)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnUploadPhoto = findViewById<ImageButton>(R.id.btnUploadPhoto)
        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)
        val btnNotifications = findViewById<ImageView>(R.id.btnNotifications)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        // Retrieve Token
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        accessToken = sharedPref.getString("access_token", "") ?: ""
        if (accessToken.isNotEmpty()) {
            RetrofitClient.authToken = accessToken
        }

        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load Data
        loadProfileData()

        btnBack.setOnClickListener {
            finish()
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, DentistNotificationsActivity::class.java))
        }

        btnMenu.setOnClickListener {
            startActivity(Intent(this, DentistMenuBarActivity::class.java))
        }

        btnUploadPhoto.setOnClickListener {
            Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        btnSaveChanges.setOnClickListener {
            saveProfileData()
        }
    }

    private fun loadProfileData() {
        RetrofitClient.instance.getProfile().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val user = response.body()?.user
                    user?.let {
                        val names = it.full_name.split(" ")
                        etFirstName.setText(names.getOrNull(0) ?: "")
                        etLastName.setText(names.drop(1).joinToString(" "))
                        etEmail.setText(it.email)
                        etPhone.setText(it.phone ?: "")
                        etClinicAddress.setText(it.clinic_address ?: "")
                        
                        tvProfileName.text = it.full_name
                        tvProfileSpecialization.text = it.specialization ?: "General Dentist"

                        // Update stored name in case it changed
                        getSharedPreferences("SmileAI", MODE_PRIVATE).edit()
                            .putString("user_name", it.full_name)
                            .apply()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    android.util.Log.e("DentistProfile", "Failed to load profile: ${response.code()} - $errorBody")
                    Toast.makeText(this@DentistProfileActivity, "Failed to load profile (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val fullName = "$firstName $lastName"
        val phone = etPhone.text.toString().trim()
        val address = etClinicAddress.text.toString().trim()

        val updateData = mutableMapOf(
            "full_name" to fullName,
            "phone" to phone,
            "clinic_address" to address
        )
        
        // Also capture specialization from TV if it was changed/loaded
        val spec = tvProfileSpecialization.text.toString()
        if (spec.isNotEmpty() && spec != "General Dentist") {
            updateData["specialization"] = spec
        }

        RetrofitClient.instance.updateProfile(updateData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DentistProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    tvProfileName.text = fullName
                    
                    // Update stored name
                    getSharedPreferences("SmileAI", MODE_PRIVATE).edit()
                        .putString("user_name", fullName)
                        .apply()

                    finish()
                } else {
                    Toast.makeText(this@DentistProfileActivity, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
