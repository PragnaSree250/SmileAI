package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistSignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_sign_up)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etSpecialization = findViewById<EditText>(R.id.etSpecialization)
        val etClinicAddress = findViewById<EditText>(R.id.etClinicAddress)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        btnBack.setOnClickListener {
            finish()
        }

        tvSignIn.setOnClickListener {
            finish() // Go back to Login
        }

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val specialization = etSpecialization.text.toString().trim()
            val clinicAddress = etClinicAddress.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Request Data
            val requestData = mapOf(
                "full_name" to name,
                "email" to email,
                "specialization" to specialization,
                "clinic_address" to clinicAddress,
                "password" to password,
                "role" to "dentist" // ✅ ADDED ROLE
            )

            // Perform API Call
            RetrofitClient.instance.signUp(requestData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@DentistSignUpActivity, "Sign Up Successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@DentistSignUpActivity, DentistDashboardActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = if (!errorBody.isNullOrEmpty()) {
                            if (errorBody.contains("Already existed")) "Already existed"
                            else if (errorBody.contains("message")) {
                                errorBody.substringAfter("\"message\":\"").substringBefore("\"")
                            } else errorBody
                        } else response.body()?.message ?: "Sign up failed"
                        
                        Toast.makeText(this@DentistSignUpActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@DentistSignUpActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
