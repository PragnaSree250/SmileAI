package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PatientLogoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_logout)

        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val btnCancel = findViewById<TextView>(R.id.btnCancel)
        val btnLogoutConfirm = findViewById<AppCompatButton>(R.id.btnLogoutConfirm)

        btnClose.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnLogoutConfirm.setOnClickListener {
            val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
            sharedPref.edit().clear().apply() // Clear session
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finishAffinity()
        }
    }
}
