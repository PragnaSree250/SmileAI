package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.Case
import com.simats.smileai.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DentistDashboardActivity : ComponentActivity() {
    private lateinit var tvWelcomeName: TextView
    private lateinit var recentCasesContainer: LinearLayout
    private lateinit var tvActiveCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvDoneCount: TextView
    private lateinit var tvPatientsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_dashboard)

        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        recentCasesContainer = findViewById(R.id.recentCasesContainer)
        tvActiveCount = findViewById(R.id.tvActiveCount)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvDoneCount = findViewById(R.id.tvDoneCount)
        tvPatientsCount = findViewById(R.id.tvPatientsCount)

        val btnNewCase = findViewById<Button>(R.id.btnNewCase)
        val btnViewAll = findViewById<TextView>(R.id.btnViewAll)
        val btnAddPatient = findViewById<LinearLayout>(R.id.btnAddPatient)
        val btnSchedule = findViewById<LinearLayout>(R.id.btnSchedule)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnProfile = findViewById<ImageView>(R.id.btnProfile)

        // Load saved name
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)
        if (token != null) {
            RetrofitClient.authToken = token
        }
        
        val userName = sharedPref.getString("user_name", "Doctor")
        tvWelcomeName.text = "Welcome, Dr. $userName"

        btnProfile.setOnClickListener {
            startActivity(Intent(this, DentistProfileActivity::class.java))
        }

        btnNewCase.setOnClickListener {
            startActivity(Intent(this, DentistNewCase1Activity::class.java))
        }

        btnViewAll.setOnClickListener {
            startActivity(Intent(this, DentistAllCasesActivity::class.java))
        }

        btnAddPatient.setOnClickListener {
            startActivity(Intent(this, DentistNewCase1Activity::class.java))
        }

        btnSchedule.setOnClickListener {
            startActivity(Intent(this, DentistSchedule::class.java))
        }
        
        
        btnMenu.setOnClickListener {
             startActivity(Intent(this, DentistMenuBarActivity::class.java))
        }

        // Stats Card Interactivity
        val cardActive = tvActiveCount.parent as View
        val cardPending = tvPendingCount.parent as View
        val cardDone = tvDoneCount.parent as View
        val cardPatients = tvPatientsCount.parent as View

        val toAllCases = { filter: String? ->
            val intent = Intent(this, DentistAllCasesActivity::class.java)
            if (filter != null) intent.putExtra("FILTER_TYPE", filter)
            startActivity(intent)
        }

        cardActive.setOnClickListener { toAllCases("Active") }
        cardPending.setOnClickListener { toAllCases("Pending") }
        cardDone.setOnClickListener { toAllCases("Done") }
        cardPatients.setOnClickListener { toAllCases(null) }

        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh name in case it was updated in Profile
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Doctor")
        tvWelcomeName.text = "Welcome, Dr. $userName"
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", "") ?: ""

        if (token.isEmpty()) return

        RetrofitClient.instance.getDentistCases().enqueue(object : Callback<List<Case>> {
            override fun onResponse(call: Call<List<Case>>, response: Response<List<Case>>) {
                if (response.isSuccessful) {
                    val cases = response.body() ?: emptyList()
                    updateStats(cases)
                    displayRecentCases(cases.take(5))
                }
            }

            override fun onFailure(call: Call<List<Case>>, t: Throwable) {
                // Silently fail or log
            }
        })
    }

    private fun updateStats(cases: List<Case>) {
        val active = cases.count { it.status?.lowercase() == "active" || it.status?.lowercase() == "in progress" }
        val pending = cases.count { it.status?.lowercase() == "pending" }
        val done = cases.count { it.status?.lowercase() == "completed" || it.status?.lowercase() == "done" }
        val uniquePatients = cases.map { "${it.patient_first_name} ${it.patient_last_name}" }.distinct().size

        tvActiveCount.text = active.toString()
        tvPendingCount.text = pending.toString()
        tvDoneCount.text = done.toString()
        tvPatientsCount.text = uniquePatients.toString()
    }

    private fun displayRecentCases(cases: List<Case>) {
        recentCasesContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (cases.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No recent cases"
                setTextColor(resources.getColor(R.color.white, null))
                setPadding(0, 20, 0, 0)
            }
            recentCasesContainer.addView(emptyTv)
            return
        }

        for (case in cases) {
            val itemView = inflater.inflate(R.layout.item_recent_case_dashboard, recentCasesContainer, false)
            
            val tvInitial = itemView.findViewById<TextView>(R.id.tvCaseInitial)
            val tvName = itemView.findViewById<TextView>(R.id.tvCasePatientName)
            val tvDetail = itemView.findViewById<TextView>(R.id.tvCaseDetail)
            val tvStatus = itemView.findViewById<TextView>(R.id.tvCaseStatus)

            val fullName = "${case.patient_first_name} ${case.patient_last_name}"
            tvName.text = fullName
            tvInitial.text = case.patient_first_name?.take(1)?.uppercase() ?: "P"
            tvDetail.text = "${case.restoration_type ?: "Case"} ${case.tooth_numbers ?: ""}"
            tvStatus.text = case.status ?: "Active"

            // Set status background based on status
            when (case.status?.lowercase()) {
                "completed", "done" -> tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                "pending" -> tvStatus.setBackgroundResource(R.drawable.bg_status_yellow)
                else -> tvStatus.setBackgroundResource(R.drawable.bg_status_blue)
            }

            itemView.setOnClickListener {
                val intent = Intent(this, DentistCasesOverallReportActivity::class.java)
                intent.putExtra("EXTRA_CASE_ID", case.id)
                startActivity(intent)
            }

            recentCasesContainer.addView(itemView)
        }
    }
}
