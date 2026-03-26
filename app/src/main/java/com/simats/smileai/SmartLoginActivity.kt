package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmartLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_login)

        val etIdentifier = findViewById<EditText>(R.id.etIdentifier)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvRoleHint = findViewById<TextView>(R.id.tvRoleHint)
        val cbRememberMe = findViewById<CheckBox>(R.id.cbRememberMe)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvForgot = findViewById<TextView>(R.id.tvForgot)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // Remember Me Logic
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val savedId = sharedPref.getString("remember_identifier", "")
        val savedPass = sharedPref.getString("remember_password", "")
        val isRemembered = sharedPref.getBoolean("is_remembered", false)

        if (isRemembered) {
            etIdentifier.setText(savedId)
            etPassword.setText(savedPass)
            cbRememberMe.isChecked = true
            updateRoleHint(savedId ?: "")
        }

        etIdentifier.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRoleHint(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnBack.setOnClickListener { finish() }

        tvForgot.setOnClickListener {
            // Default to Dentist Reset for now, or unified if available
            startActivity(Intent(this, DentistResetPasswordActivity::class.java))
        }

        tvSignUp.setOnClickListener {
            showSignUpDialog()
        }

        btnSignIn.setOnClickListener {
            val identifier = etIdentifier.text.toString().trim()
            val password = etPassword.text.toString()
            val rememberMe = cbRememberMe.isChecked

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your Email/ID and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestData = mapOf("email" to identifier, "password" to password)

            val progressDialog = android.app.ProgressDialog(this).apply {
                setMessage("Authenticating...")
                setCancelable(false)
                show()
            }

            RetrofitClient.instance.login(requestData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            val user = body.user
                            val token = body.access_token

                            if (user != null && token != null) {
                                val fullToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                                RetrofitClient.authToken = fullToken
                                
                                with(sharedPref.edit()) {
                                    putInt("user_id", user.id)
                                    putString("access_token", fullToken)
                                    putString("user_role", user.role)
                                    putString("user_name", user.full_name)
                                    putString("patient_clinical_id", user.patient_id ?: "")
                                    putString("dentist_clinical_id", user.dentist_id ?: "")
                                    
                                    if (rememberMe) {
                                        putString("remember_identifier", identifier)
                                        putString("remember_password", password)
                                        putBoolean("is_remembered", true)
                                    } else {
                                        remove("remember_identifier")
                                        remove("remember_password")
                                        putBoolean("is_remembered", false)
                                    }
                                    apply()
                                }

                                val intent = if (user.role?.lowercase() == "dentist") {
                                    Intent(this@SmartLoginActivity, DentistDashboardActivity::class.java)
                                } else {
                                    Intent(this@SmartLoginActivity, PatientDashboardActivity::class.java)
                                }
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@SmartLoginActivity, body?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SmartLoginActivity, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@SmartLoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateRoleHint(input: String) {
        val hintView = findViewById<TextView>(R.id.tvRoleHint)
        hintView.text = "Enter Email or ID to continue"
        hintView.setTextColor(android.graphics.Color.parseColor("#94a3b8"))
    }

    private fun showSignUpDialog() {
        val options = arrayOf("I am a Patient", "I am a Dentist")
        AlertDialog.Builder(this)
            .setTitle("Join SmileAI as...")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, PatientSignUpActivity::class.java))
                    1 -> startActivity(Intent(this, DentistSignUpActivity::class.java))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
