package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RoleSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val cardDentist = findViewById<LinearLayout>(R.id.cardDentist)
        val cardPatient = findViewById<LinearLayout>(R.id.cardPatient)
        val btnAbout = findViewById<TextView>(R.id.btnAbout)

        cardDentist.setOnClickListener {
            startActivity(Intent(this, DentistLoginActivity::class.java))
        }

        cardPatient.setOnClickListener {
            startActivity(Intent(this, PatientLoginActivity::class.java))
        }

        btnAbout.setOnClickListener {
            Toast.makeText(this, "About Smile AI", Toast.LENGTH_SHORT).show()
        }
    }
}
