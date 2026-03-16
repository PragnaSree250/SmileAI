package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_login)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val etPatientId = findViewById<EditText>(R.id.etPatientId)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbRememberMe = findViewById<CheckBox>(R.id.cbRememberMe)

        // Load "Remember Me" data
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val savedEmail = sharedPref.getString("remember_email_patient", "")
        val savedPassword = sharedPref.getString("remember_password_patient", "")
        val isRemembered = sharedPref.getBoolean("is_remembered_patient", false)

        if (isRemembered) {
            etPatientId.setText(savedEmail)
            etPassword.setText(savedPassword)
            cbRememberMe.isChecked = true
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSignIn.setOnClickListener {
            val email = etPatientId.text.toString().trim()
            val password = etPassword.text.toString()
            val rememberMe = cbRememberMe.isChecked

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestData = mapOf("email" to email, "password" to password)

            RetrofitClient.instance.login(requestData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            val user = body.user
                            val token = body.access_token

                            Log.d("LOGIN_DEBUG", "User Role: ${user?.role}")

                            if (user != null && token != null) {
                                with(sharedPref.edit()) {
                                    putInt("user_id", user.id)
                                    putString("access_token", "Bearer $token")
                                    putString("user_role", user.role)
                                    putString("patient_clinical_id", user.patient_id ?: "")
                                    putString("user_name", user.full_name)
                                    
                                    // Handle Remember Me
                                    if (rememberMe) {
                                        putString("remember_email_patient", email)
                                        putString("remember_password_patient", password)
                                        putBoolean("is_remembered_patient", true)
                                    } else {
                                        remove("remember_email_patient")
                                        remove("remember_password_patient")
                                        putBoolean("is_remembered_patient", false)
                                    }
                                    apply()
                                }

                                val intent = if (user.role?.lowercase() == "dentist") {
                                    Intent(this@PatientLoginActivity, DentistDashboardActivity::class.java)
                                } else {
                                    Intent(this@PatientLoginActivity, PatientDashboardActivity::class.java)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@PatientLoginActivity, "Invalid user data from server", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@PatientLoginActivity, body?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        var errorMessage = "Invalid Credentials"
                        try {
                            val errorString = response.errorBody()?.string()
                            if (!errorString.isNullOrEmpty()) {
                                val jsonObject = org.json.JSONObject(errorString)
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.getString("message")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Toast.makeText(this@PatientLoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@PatientLoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, PatientResetPasswordActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSignUp).setOnClickListener {
            startActivity(Intent(this, PatientSignUpActivity::class.java))
        }
    }
}
