package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class CommonLogOutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_log_out)

        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val btnCancel = findViewById<TextView>(R.id.btnCancel)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnClose.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() 
        }
    }
}
