package com.simats.smileai

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DentistSchedule : ComponentActivity() {
    private var selectedCase: com.simats.smileai.network.Case? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dentist_schedule)

        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<android.widget.Button>(R.id.btnScheduleAction).setOnClickListener {
            showDatePicker()
        }

        findViewById<android.widget.Button>(R.id.btnDownloadCaseReport).setOnClickListener {
            selectedCase?.let {
                val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                val token = sharedPref.getString("access_token", "") ?: ""
                val url = "${com.simats.smileai.network.RetrofitClient.BASE_URL}cases/${it.id}/download/report/pdf"
                com.simats.smileai.utils.FileDownloader.downloadFile(this, url, "Report_Case_${it.id}.pdf", token)
            } ?: run {
                Toast.makeText(this, "Please select a case first", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<android.widget.ImageView>(R.id.btnCalendar).setOnClickListener {
            showDatePicker()
        }
        
        // Fetch cases and pick the first one as default for scheduling demo.
        fetchCases()
    }

    private fun fetchCases() {
        val targetCaseId = intent.getIntExtra("EXTRA_CASE_ID", -1)
        com.simats.smileai.network.RetrofitClient.instance.getDentistCases().enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Case>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, response: retrofit2.Response<List<com.simats.smileai.network.Case>>) {
                val cases = response.body() ?: emptyList()
                selectedCase = if (targetCaseId != -1) {
                    cases.find { it.id == targetCaseId }
                } else {
                    cases.firstOrNull()
                }

                selectedCase?.let {
                    findViewById<TextView>(R.id.tvSchedPatientName).text = "${it.patient_first_name} ${it.patient_last_name}"
                    findViewById<TextView>(R.id.tvSchedCaseId).text = "#${it.id}"
                    findViewById<TextView>(R.id.tvSchedDate).text = it.created_at?.split(" ")?.firstOrNull() ?: it.created_at ?: "N/A"
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Case>>, t: Throwable) {}
        })
    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        val datePicker = android.app.DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = "$year-${month + 1}-$day"
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(java.util.GregorianCalendar(year, month, day).time)
            scheduleAppointment(selectedDate, dayName)
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun scheduleAppointment(date: String, day: String) {
        val case = selectedCase ?: return
        val appointment = com.simats.smileai.network.Appointment(
            case_id = case.id ?: -1,
            patient_id = case.patient_id ?: "",
            appointment_date = date,
            appointment_day = day
        )

        com.simats.smileai.network.RetrofitClient.instance.createAppointment(appointment).enqueue(object : retrofit2.Callback<com.simats.smileai.network.ApiResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, response: retrofit2.Response<com.simats.smileai.network.ApiResponse>) {
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(this@DentistSchedule, "Appointment Fixed & Patient Notified!", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.smileai.network.ApiResponse>, t: Throwable) {}
        })
    }
}
