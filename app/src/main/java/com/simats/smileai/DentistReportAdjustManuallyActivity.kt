package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.app.AlertDialog

class DentistReportAdjustManuallyActivity : ComponentActivity() {

    private var prescribedMed: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_report_adjust_manually)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnReset = findViewById<LinearLayout>(R.id.btnReset)
        val btnApplyChanges = findViewById<Button>(R.id.btnApplyChanges)

        val etReasoning = findViewById<android.widget.EditText>(R.id.etAdjustReasoning)
        val etRisk = findViewById<android.widget.EditText>(R.id.etAdjustRisk)
        val etPrognosis = findViewById<android.widget.EditText>(R.id.etAdjustPrognosis)
        val etPlacement = findViewById<android.widget.EditText>(R.id.etAdjustPlacement)
        val etHyperdontia = findViewById<android.widget.EditText>(R.id.etAdjustHyperdontia)
        val etSymmetry = findViewById<android.widget.EditText>(R.id.etAdjustSymmetry)
        val etGoldenRatio = findViewById<android.widget.EditText>(R.id.etAdjustGoldenRatio)

        // Pre-populate with passed AI suggestions
        etReasoning.setText(intent.getStringExtra("EXTRA_AI_REASONING"))
        etRisk.setText(intent.getStringExtra("EXTRA_AI_RISK"))
        etPrognosis.setText(intent.getStringExtra("EXTRA_AI_PROGNOSIS"))
        etPlacement.setText(intent.getStringExtra("EXTRA_AI_PLACEMENT"))
        etHyperdontia.setText(intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS") ?: "None")
        etSymmetry.setText(intent.getStringExtra("EXTRA_AI_SYMMETRY") ?: "Optimal")
        etGoldenRatio.setText(intent.getStringExtra("EXTRA_AI_GOLDEN_RATIO") ?: "1.618 Match")

        btnBack.setOnClickListener {
            finish()
        }

        btnReset.setOnClickListener {
            Toast.makeText(this, "Resetting to AI recommendations...", Toast.LENGTH_SHORT).show()
            etReasoning.setText(intent.getStringExtra("EXTRA_AI_REASONING"))
            etRisk.setText(intent.getStringExtra("EXTRA_AI_RISK"))
            etPrognosis.setText(intent.getStringExtra("EXTRA_AI_PROGNOSIS"))
            etPlacement.setText(intent.getStringExtra("EXTRA_AI_PLACEMENT"))
            etHyperdontia.setText(intent.getStringExtra("EXTRA_HYPERDONTIA_STATUS"))
            etSymmetry.setText(intent.getStringExtra("EXTRA_AI_SYMMETRY"))
            etGoldenRatio.setText(intent.getStringExtra("EXTRA_AI_GOLDEN_RATIO"))
        }

        val btnPrescribeMedication = findViewById<Button>(R.id.btnPrescribeMedication)
        btnPrescribeMedication.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_medication, null)
            val etMedName = dialogView.findViewById<android.widget.EditText>(R.id.etMedName)
            val etMedDosage = dialogView.findViewById<android.widget.EditText>(R.id.etMedDosage)
            val etMedFrequency = dialogView.findViewById<android.widget.EditText>(R.id.etMedFrequency)
            val etMedDuration = dialogView.findViewById<android.widget.EditText>(R.id.etMedDuration)
            val etMedNotes = dialogView.findViewById<android.widget.EditText>(R.id.etMedNotes)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    prescribedMed = Bundle().apply {
                        putString("name", etMedName.text.toString())
                        putString("dosage", etMedDosage.text.toString())
                        putString("frequency", etMedFrequency.text.toString())
                        putString("duration", etMedDuration.text.toString())
                        putString("notes", etMedNotes.text.toString())
                    }
                    Toast.makeText(this, "Medication drafted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnApplyChanges.setOnClickListener {
            Toast.makeText(this, "Changes Applied", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DentistApplyAndPreviewActivity::class.java).apply {
                putExtras(getIntent())
                putExtra("EXTRA_AI_REASONING", etReasoning.text.toString())
                putExtra("EXTRA_AI_RISK", etRisk.text.toString())
                putExtra("EXTRA_AI_PROGNOSIS", etPrognosis.text.toString())
                putExtra("EXTRA_AI_PLACEMENT", etPlacement.text.toString())
                putExtra("EXTRA_HYPERDONTIA_STATUS", etHyperdontia.text.toString())
                putExtra("EXTRA_MISSING_TEETH_STATUS", intent.getStringExtra("EXTRA_MISSING_TEETH_STATUS"))
                putExtra("EXTRA_AI_SYMMETRY", etSymmetry.text.toString())
                putExtra("EXTRA_AI_GOLDEN_RATIO", etGoldenRatio.text.toString())
                prescribedMed?.let { putExtra("EXTRA_MEDICATION_BUNDLE", it) }
            }
            startActivity(intent)
            finish()
        }
    }
}
