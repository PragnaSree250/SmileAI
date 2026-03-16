package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientSetNewPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_set_new_password)

        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)

        val email = intent.getStringExtra("email") ?: ""
        val otp = intent.getStringExtra("otp") ?: ""

        btnBack.setOnClickListener {
            finish()
        }

        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = mapOf(
                "email" to email,
                "otp" to otp,
                "new_password" to newPassword
            )

            RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@PatientSetNewPasswordActivity, "Password reset successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@PatientSetNewPasswordActivity, PatientLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = response.body()?.message ?: "Reset failed"
                        Toast.makeText(this@PatientSetNewPasswordActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@PatientSetNewPasswordActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
