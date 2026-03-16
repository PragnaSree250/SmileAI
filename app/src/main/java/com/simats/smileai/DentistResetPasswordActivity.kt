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

class DentistResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_reset_password)

        val btnBack = findViewById<LinearLayout>(R.id.btnBackToLogin)
        val btnSend = findViewById<Button>(R.id.btnSendReset)
        val etEmail = findViewById<EditText>(R.id.etEmailReset)

        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Request Data
            val requestData = mapOf("email" to email)

            // Perform API Call
            RetrofitClient.instance.forgotPassword(requestData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@DentistResetPasswordActivity, "Reset code generated", Toast.LENGTH_LONG).show()
                        
                        // Navigate to Check Email Screen and pass email
                        val intent = Intent(this@DentistResetPasswordActivity, CheckEmailActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.body()?.message ?: "Request failed"
                        Toast.makeText(this@DentistResetPasswordActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@DentistResetPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
