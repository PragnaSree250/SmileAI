package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class CheckEmailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_email)

        val btnBack = findViewById<LinearLayout>(R.id.btnBackToLogin)
        val btnEnterCode = findViewById<Button>(R.id.btnEnterCode)

        // Retrieve email passed from ResetPassword screen
        val email = intent.getStringExtra("email") ?: ""

        btnBack.setOnClickListener {
             val intent = Intent(this, DentistLoginActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
             startActivity(intent)
             finish()
        }

        btnEnterCode.setOnClickListener {
             val intent = Intent(this, VerifyCodeActivity::class.java)
             intent.putExtra("email", email) // Pass email forward
             startActivity(intent)
        }
    }
}
