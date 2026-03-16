package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        val sharedPref = getSharedPreferences("SmileAI", android.content.Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Patient") ?: "Patient"
        val firstName = userName.split(" ").firstOrNull() ?: "Patient"
        
        val tvGreeting = findViewById<TextView>(R.id.tvPatientGreetingName)
        tvGreeting.text = "$firstName \uD83D\uDC4B"

        val btnNewReports = findViewById<LinearLayout>(R.id.btnNewReports)
        val btnMeds = findViewById<LinearLayout>(R.id.btnMeds)
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navReports = findViewById<LinearLayout>(R.id.navReports)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        val recentActivity1 = findViewById<LinearLayout>(R.id.recentActivity1)
        val recentActivity2 = findViewById<LinearLayout>(R.id.recentActivity2)
        val btnActiveCases = findViewById<LinearLayout>(R.id.btnActiveCases)
        val btnNotifications = findViewById<ImageView>(R.id.btnNotifications)
        val btnUnread = findViewById<LinearLayout>(R.id.btn_unread)

        btnNewReports.setOnClickListener {
            startActivity(Intent(this, PatientReportActivity::class.java))
        }

        btnMeds.setOnClickListener {
            startActivity(Intent(this, PatientMedActivity::class.java))
        }

        navHome.setOnClickListener {
            // Already on Home
        }

        navCases.setOnClickListener {
            startActivity(android.content.Intent(this, PatientCaseAllActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navReports.setOnClickListener {
            startActivity(Intent(this, PatientReportActivity::class.java))
        }

        navProfile.setOnClickListener {
            val profileIcon = navProfile.getChildAt(0) as ImageView
            val profileText = navProfile.getChildAt(1) as TextView
            profileIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
            profileText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            startActivity(Intent(this, PatientProfileActivity::class.java))
        }

        recentActivity1.setOnClickListener {
            startActivity(Intent(this, PatientHealingActivity::class.java))
        }

        recentActivity2.setOnClickListener {
            startActivity(Intent(this, PatientRecentActivity::class.java))
        }

        btnActiveCases.setOnClickListener {
            startActivity(Intent(this, PatientCaseActiveActivity::class.java))
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, PatientNotificationsActivity::class.java))
        }

        btnUnread.setOnClickListener {
            startActivity(Intent(this, PatientNotificationsActivity::class.java))
        }

        loadStats()
    }

    private fun loadStats() {
        RetrofitClient.instance.getPatientStats().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        findViewById<TextView>(R.id.tvActiveCasesCount).text = body.active_cases?.toString() ?: "0"
                        findViewById<TextView>(R.id.tvNewReportsCount).text = body.total_reports?.toString() ?: "0"
                        findViewById<TextView>(R.id.tvUnreadCount).text = body.unread_notifications?.toString() ?: "0"
                    }
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Ignore for now
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }
}
