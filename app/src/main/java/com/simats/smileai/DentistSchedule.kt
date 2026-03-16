package com.simats.smileai

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DentistSchedule : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dentist_schedule)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val tvTodayDate = findViewById<TextView>(R.id.tvTodayDate)

        // Set current date
        val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        tvTodayDate.text = sdf.format(Date())

        btnBack.setOnClickListener {
            finish()
        }
    }
}
