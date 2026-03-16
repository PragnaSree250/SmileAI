package com.simats.smileai

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PatientHealingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_healing)

        val rvLogs = findViewById<RecyclerView>(R.id.rvHealingLogs)
        val btnAddLog = findViewById<Button>(R.id.btnAddLog)

        rvLogs.layoutManager = LinearLayoutManager(this)
        
        // Simple mock data for college project
        Toast.makeText(this, "Loading recovery history...", Toast.LENGTH_SHORT).show()

        btnAddLog.setOnClickListener {
            Toast.makeText(this, "Opening Camera for daily photo...", Toast.LENGTH_SHORT).show()
            // In real app, this would open CameraActivity and POST to /healing-logs
        }
    }
}
