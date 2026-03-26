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

class PatientSignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_sign_up)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
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
            val password = etPassword.text.toString()

            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith(".com")) {
                Toast.makeText(this, "Please enter a valid email address ending in .com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Request Data
            val requestData = mutableMapOf(
                "full_name" to name,
                "email" to email,
                "password" to password,
                "role" to "patient"
            )

            // Perform API Call
            RetrofitClient.instance.signUp(requestData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@PatientSignUpActivity, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@PatientSignUpActivity, SmartLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = if (!errorBody.isNullOrEmpty()) {
                            if (errorBody.contains("Already existed")) "Already existed"
                            else if (errorBody.contains("message")) {
                                // Basic attempt to parse JSON message if available
                                errorBody.substringAfter("\"message\":\"").substringBefore("\"")
                            } else errorBody
                        } else response.body()?.message ?: "Sign up failed"
                        
                        Toast.makeText(this@PatientSignUpActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@PatientSignUpActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
