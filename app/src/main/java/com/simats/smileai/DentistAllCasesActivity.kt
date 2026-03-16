package com.simats.smileai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
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

class DentistAllCasesActivity : ComponentActivity() {
    private lateinit var casesContainer: LinearLayout
    private lateinit var staticCasesList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_all_cases)

        casesContainer = findViewById(R.id.casesContainer)
        staticCasesList = findViewById(R.id.staticCasesList)

        val btnNewCase = findViewById<Button>(R.id.btnNewCase)
        val btnFilter = findViewById<LinearLayout>(R.id.btnFilter)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        
        val btnNotification = findViewById<ImageView>(R.id.btnNotification)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        // Restore token
        val sharedPref = getSharedPreferences("SmileAI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)
        if (token != null) {
            RetrofitClient.authToken = token
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnNewCase.setOnClickListener {
            startActivity(Intent(this, DentistNewCase1Activity::class.java))
        }

        btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter Clicked", Toast.LENGTH_SHORT).show()
        }
        
        btnNotification?.setOnClickListener {
            startActivity(Intent(this, DentistNotificationsActivity::class.java))
        }
        
        btnMenu?.setOnClickListener {
            startActivity(Intent(this, DentistMenuBarActivity::class.java))
        }

        fetchCases()
    }

    private fun fetchCases() {
        RetrofitClient.instance.getDentistCases().enqueue(object : Callback<List<Case>> {
            override fun onResponse(call: Call<List<Case>>, response: Response<List<Case>>) {
                if (response.isSuccessful) {
                    val cases = response.body() ?: emptyList()
                    if (cases.isNotEmpty()) {
                        staticCasesList.visibility = android.view.View.GONE
                        updateCasesUi(cases)
                    }
                }
            }

            override fun onFailure(call: Call<List<Case>>, t: Throwable) {
                Toast.makeText(this@DentistAllCasesActivity, "Error fetching cases: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCasesUi(cases: List<Case>) {
        casesContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (case in cases) {
            val itemView = inflater.inflate(R.layout.item_case_all_cases, casesContainer, false)
            
            val tvCaseId = itemView.findViewById<TextView>(R.id.tvCaseId)
            val tvFirstName = itemView.findViewById<TextView>(R.id.tvPatientFirstName)
            val tvLastName = itemView.findViewById<TextView>(R.id.tvPatientLastName)
            val tvType = itemView.findViewById<TextView>(R.id.tvRestorationType)
            val tvTooth = itemView.findViewById<TextView>(R.id.tvToothNumber)
            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

            tvCaseId.text = "C-${case.id ?: "????"}"
            tvFirstName.text = case.patient_first_name
            tvLastName.text = case.patient_last_name
            tvType.text = case.restoration_type ?: "Case"
            tvTooth.text = case.tooth_numbers ?: "-"
            tvStatus.text = case.status ?: "Active"

            // Set status background
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

            casesContainer.addView(itemView)
        }
    }
}
