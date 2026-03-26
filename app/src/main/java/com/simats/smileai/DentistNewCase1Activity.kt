package com.simats.smileai

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DentistNewCase1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_new_case_1)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnPrevious = findViewById<TextView>(R.id.btnPrevious)
        
        val etPatientId = findViewById<EditText>(R.id.etPatientId)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etDob = findViewById<EditText>(R.id.etDob)
        val etScanId = findViewById<EditText>(R.id.etScanId)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)

        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter

        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)
                etDob.setText("$year-$formattedMonth-$formattedDay")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnBack?.setOnClickListener { finish() }
        btnPrevious?.setOnClickListener { finish() }

        btnContinue?.setOnClickListener {
            val patientId = etPatientId.text.toString().trim().uppercase().replace("-", "")
            val name = etFullName.text.toString().trim()
            val dob = etDob.text.toString()
            if (name.isEmpty() || dob.isEmpty() || patientId.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Move to Step 2: Treatment Plan (NewCase3)
            val intent = Intent(this, DentistNewCase3Activity::class.java).apply {
                putExtra("EXTRA_PATIENT_ID", patientId)
                putExtra("EXTRA_PATIENT_NAME", name)
                putExtra("EXTRA_PATIENT_DOB", dob)
                putExtra("EXTRA_SCAN_ID", etScanId.text.toString())
                putExtra("EXTRA_GENDER", if (spinnerGender.selectedItemPosition > 0) spinnerGender.selectedItem.toString() else "Female")
            }
            startActivity(intent)
        }
    }
}
