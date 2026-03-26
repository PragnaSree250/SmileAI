package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity

class DentistSolutionSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_solution_selection)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val tvDeficiency = findViewById<TextView>(R.id.tvSolutionDeficiency)
        val spinnerRestoration = findViewById<Spinner>(R.id.spinnerRestoration)
        val spinnerMaterial = findViewById<Spinner>(R.id.spinnerMaterial)
        val etNotes = findViewById<EditText>(R.id.etSolutionNotes)
        val btnProceed = findViewById<Button>(R.id.btnProceed)

        btnBack.setOnClickListener { finish() }

        val deficiency = intent.getStringExtra("EXTRA_AI_DEFICIENCY") ?: intent.getStringExtra("EXTRA_DEFICIENCY") ?: "General"
        val tooth = intent.getStringExtra("EXTRA_TOOTH") ?: ""
        tvDeficiency.text = "$deficiency detected on $tooth"

        // Setup Spinners
        val restorations = arrayOf("Full Crown", "Bridge", "Veneer", "Inlay/Onlay", "Implant Crown")
        val materials = arrayOf("Zirconia", "Pfm", "E-max", "Composite", "Gold")

        spinnerRestoration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, restorations)
        spinnerMaterial.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, materials)

        // Set default from AI if available
        val suggestedRest = intent.getStringExtra("EXTRA_AI_SUGGESTED_RESTORATION") ?: ""
        val suggestedMat = intent.getStringExtra("EXTRA_AI_SUGGESTED_MATERIAL") ?: ""
        
        if (suggestedRest.isNotEmpty()) {
            val idx = restorations.indexOfFirst { it.contains(suggestedRest, ignoreCase = true) }
            if (idx != -1) spinnerRestoration.setSelection(idx)
        }
        if (suggestedMat.isNotEmpty()) {
            val idx = materials.indexOfFirst { it.contains(suggestedMat, ignoreCase = true) }
            if (idx != -1) spinnerMaterial.setSelection(idx)
        }

        val btnOpenSmileLibrary = findViewById<LinearLayout>(R.id.btnOpenSmileLibrary)
        val tvSelectedSmileShape = findViewById<TextView>(R.id.tvSelectedSmileShape)

        btnOpenSmileLibrary.setOnClickListener {
            val intent = Intent(this, DentistSmileLibraryActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        btnProceed.setOnClickListener {
            val selectedRestoration = spinnerRestoration.selectedItem.toString()
            val selectedMaterial = spinnerMaterial.selectedItem.toString()
            val notes = etNotes.text.toString()
            val selectedShape = tvSelectedSmileShape.text.toString()

            val intent = Intent(this, DentistApplyAndPreviewActivity::class.java).apply {
                putExtras(this@DentistSolutionSelectionActivity.intent)
                putExtra("EXTRA_RESTORATION", selectedRestoration)
                putExtra("EXTRA_MATERIAL", selectedMaterial)
                putExtra("EXTRA_AI_RECOMMENDED_SHAPE", if (selectedShape.contains("Select")) "Ovoid-Tapering Hybrid" else selectedShape)
                putExtra("EXTRA_AI_RECOMMENDATION", "Plan: $selectedRestoration ($selectedMaterial). Aesthetic: $selectedShape. $notes")
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val shape = data?.getStringExtra("SELECTED_SMILE_SHAPE")
            findViewById<TextView>(R.id.tvSelectedSmileShape).text = shape ?: "Ovoid-Tapering Hybrid"
        }
    }
}
