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
    private var latestCaseId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        val sharedPref = getSharedPreferences("SmileAI", android.content.Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Patient") ?: "Patient"
        val firstName = userName.split(" ").firstOrNull() ?: "Patient"
        
        val tvGreeting = findViewById<TextView>(R.id.tvPatientGreetingName)
        tvGreeting.text = "$firstName \uD83D\uDC4B ✨ 🦷"

        // Ensure RetrofitClient is initialized
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        if (accessToken.isNotEmpty() && RetrofitClient.authToken == null) {
            RetrofitClient.authToken = accessToken
        }

        val btnNewReports = findViewById<LinearLayout>(R.id.btnNewReports)
        val btnMeds = findViewById<LinearLayout>(R.id.btnMeds)
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navReports = findViewById<LinearLayout>(R.id.navReports)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        val recentActivity1 = findViewById<LinearLayout>(R.id.recentActivity1)
        val btnActiveCases = findViewById<LinearLayout>(R.id.btnActiveCases)
        val btnNotifications = findViewById<ImageView>(R.id.btnNotifications)
        val btnUnread = findViewById<LinearLayout>(R.id.btn_unread)

        btnNewReports.setOnClickListener {
            val intent = Intent(this, PatientReportActivity::class.java)
            latestCaseId?.let { intent.putExtra("EXTRA_CASE_ID", it) }
            startActivity(intent)
        }

        btnMeds.setOnClickListener {
            val intent = Intent(this, PatientMedActivity::class.java)
            latestCaseId?.let { intent.putExtra("EXTRA_CASE_ID", it) }
            startActivity(intent)
        }

        navHome.setOnClickListener {
            // Already on Home
        }

        navCases.setOnClickListener {
            startActivity(android.content.Intent(this, PatientCaseAllActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navReports.setOnClickListener {
            startActivity(Intent(this, PatientCaseAllActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
        }

        recentActivity1.setOnClickListener {
            startActivity(Intent(this, PatientCaseAllActivity::class.java))
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

        loadLatestCase()
        loadStats()
    }

    private fun loadLatestCase() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""
        if (patientId.isEmpty()) return

        RetrofitClient.instance.getPatientCases(patientId).enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                if (response.isSuccessful) {
                    val cases = response.body() ?: emptyList()
                    val latest = cases.firstOrNull()
                    if (latest != null) {
                        latestCaseId = latest.id
                        // Status update logic for generic cases if needed
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {
                // Silent failure is okay for dashboard polling, but maybe a log
                 android.util.Log.e("PatientDashboard", "Failed to load cases: ${t.message}")
            }
        })
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
                android.util.Log.e("PatientDashboard", "Failed to load stats: ${t.message}")
            }
        })
    }

    private fun loadUpcomingAppointment() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val patientId = sharedPref.getString("patient_clinical_id", "") ?: ""
        if (patientId.isEmpty()) return

        RetrofitClient.instance.getPatientAppointments(patientId).enqueue(object : Callback<com.simats.smileai.network.AppointmentResponse> {
            override fun onResponse(call: Call<com.simats.smileai.network.AppointmentResponse>, response: Response<com.simats.smileai.network.AppointmentResponse>) {
                if (response.isSuccessful) {
                    val appointment = response.body()?.appointment
                    val llAppointment = findViewById<LinearLayout>(R.id.llUpcomingAppointment)
                    if (appointment != null) {
                        llAppointment.visibility = android.view.View.VISIBLE
                        findViewById<TextView>(R.id.tvAppointmentDate).text = appointment.appointment_date
                        findViewById<TextView>(R.id.tvAppointmentDay).text = appointment.appointment_day
                    } else {
                        llAppointment.visibility = android.view.View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<com.simats.smileai.network.AppointmentResponse>, t: Throwable) {
                android.util.Log.e("PatientDashboard", "Failed to load appointment: ${t.message}")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadLatestCase()
        loadStats()
        loadUpcomingAppointment()
    }
}
