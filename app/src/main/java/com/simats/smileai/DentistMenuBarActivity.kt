package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class DentistMenuBarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_menu_bar)

        val btnDashboard = findViewById<LinearLayout>(R.id.btnDashboard)
        val btnCases = findViewById<LinearLayout>(R.id.btnCases)
        val btnNotifications = findViewById<LinearLayout>(R.id.btnNotifications)
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfile)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        
        val tvMenuName = findViewById<TextView>(R.id.tvMenuName)
        val tvMenuInitials = findViewById<TextView>(R.id.tvMenuInitials)

        // Set dynamic name from SharedPreferences
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Doctor")
        tvMenuName?.text = "Dr. $userName"
        tvMenuInitials?.text = userName?.take(1)?.uppercase() ?: "D"

        btnClose.setOnClickListener {
            finish()
        }

        btnDashboard.setOnClickListener {
            val intent = Intent(this, DentistDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnCases.setOnClickListener {
            startActivity(Intent(this, DentistAllCasesActivity::class.java))
            finish()
        }

        btnNotifications.setOnClickListener {
             startActivity(Intent(this, DentistNotificationsActivity::class.java))
             finish()
        }

        btnProfile.setOnClickListener {
             startActivity(Intent(this, DentistProfileActivity::class.java))
             finish()
        }
        
        btnSettings.setOnClickListener {
             startActivity(Intent(this, DentistSettingsActivity::class.java))
             finish()
        }

        btnLogout.setOnClickListener {
            startActivity(Intent(this, CommonLogOutActivity::class.java))
        }
    }
}
