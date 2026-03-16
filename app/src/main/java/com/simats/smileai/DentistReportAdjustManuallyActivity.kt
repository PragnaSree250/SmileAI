package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity

class DentistReportAdjustManuallyActivity : ComponentActivity() {

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
            }
            startActivity(intent)
            finish()
        }
    }
}
