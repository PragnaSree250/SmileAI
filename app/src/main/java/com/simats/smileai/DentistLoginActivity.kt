package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
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

class DentistLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbRememberMe = findViewById<CheckBox>(R.id.cbRememberMe)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvForgot = findViewById<TextView>(R.id.tvForgot)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val savedEmail = sharedPref.getString("remember_email", "")
        val savedPassword = sharedPref.getString("remember_password", "")
        val isRemembered = sharedPref.getBoolean("is_remembered", false)

        if (isRemembered) {
            etEmail.setText(savedEmail)
            etPassword.setText(savedPassword)
            cbRememberMe.isChecked = true
        }

        btnBack.setOnClickListener {
            finish()
        }

        tvForgot.setOnClickListener {
             startActivity(Intent(this, DentistResetPasswordActivity::class.java))
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, DentistSignUpActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val rememberMe = cbRememberMe.isChecked

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestData = mapOf(
                "email" to email, 
                "password" to password
            )

            RetrofitClient.instance.login(requestData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            val user = body.user
                            val token = body.access_token
                            
                            if (user != null && token != null) {
                                RetrofitClient.authToken = token
                                with(sharedPref.edit()) {
                                    putInt("user_id", user.id)
                                    // Store only the raw token. Prefixing will happen in calls.
                                    putString("access_token", token)
                                    putString("user_role", user.role)
                                    putString("user_name", user.full_name)
                                    
                                    if (rememberMe) {
                                        putString("remember_email", email)
                                        putString("remember_password", password)
                                        putBoolean("is_remembered", true)
                                    } else {
                                        remove("remember_email")
                                        remove("remember_password")
                                        putBoolean("is_remembered", false)
                                    }
                                    apply()
                                }

                                val intent = if (user.role?.lowercase() == "patient") {
                                    Intent(this@DentistLoginActivity, PatientDashboardActivity::class.java)
                                } else {
                                    Intent(this@DentistLoginActivity, DentistDashboardActivity::class.java)
                                }
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@DentistLoginActivity, body?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@DentistLoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@DentistLoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
