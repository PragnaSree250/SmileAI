package com.simats.smileai

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.AppCompatButton

class PatientChangePasswordActivity : ComponentActivity() {

    private var isCurrentVisible = false
    private var isNewVisible = false
    private var isConfirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_change_password)

        // Header Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etCurrent = findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)

        val btnToggleCurrent = findViewById<ImageView>(R.id.btnToggleCurrent)
        val btnToggleNew = findViewById<ImageView>(R.id.btnToggleNew)
        val btnToggleConfirm = findViewById<ImageView>(R.id.btnToggleConfirm)
        val btnUpdate = findViewById<AppCompatButton>(R.id.btnUpdatePassword)

        // Toggle Visibility Helpers
        btnToggleCurrent.setOnClickListener {
            isCurrentVisible = !isCurrentVisible
            togglePasswordVisibility(etCurrent, btnToggleCurrent, isCurrentVisible)
        }

        btnToggleNew.setOnClickListener {
            isNewVisible = !isNewVisible
            togglePasswordVisibility(etNew, btnToggleNew, isNewVisible)
        }

        btnToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(etConfirm, btnToggleConfirm, isConfirmVisible)
        }

        btnUpdate.setOnClickListener {
            val current = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val confirmPass = etConfirm.text.toString()

            if (current.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulate update
            val intent = android.content.Intent(this, PatientPasswordChangeSuccessfulActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleButton: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_eye_off) // Assuming you have an off icon or reuse
            toggleButton.setColorFilter(android.graphics.Color.parseColor("#3b82f6"))
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_eye_gray)
            toggleButton.setColorFilter(android.graphics.Color.parseColor("#94a3b8"))
        }
        editText.setSelection(editText.text.length)
    }
}
