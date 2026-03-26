package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DentistNewCase3Activity : AppCompatActivity() {

    private var selectedRestoration: String? = null
    private var selectedMaterial: String? = null

    private lateinit var conditionSpinner: Spinner
    private lateinit var etToothNumbers: EditText

    private lateinit var cardCrown: LinearLayout
    private lateinit var cardBridge: LinearLayout
    private lateinit var cardVeneer: LinearLayout
    private lateinit var cardInlayOnlay: LinearLayout
    
    private lateinit var cardZirconia: LinearLayout
    private lateinit var cardLithium: LinearLayout
    private lateinit var cardPFM: LinearLayout
    private lateinit var cardPMMA: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_new_case_3)

        conditionSpinner = findViewById(R.id.conditionSpinner)
        etToothNumbers = findViewById(R.id.etToothNumbers)

        val conditionOptions = arrayOf("Select Condition", "Calculus", "Caries", "Gingivitis", "Hypodontia", "Healthy", "Tooth Discoloration")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, conditionOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        conditionSpinner.adapter = adapter

        // Initialize Restoration Cards
        cardCrown = findViewById(R.id.cardCrown)
        cardBridge = findViewById(R.id.cardBridge)
        cardVeneer = findViewById(R.id.cardVeneer)
        cardInlayOnlay = findViewById(R.id.cardInlayOnlay)

        // Initialize Material Cards
        cardZirconia = findViewById(R.id.cardZirconia)
        cardLithium = findViewById(R.id.cardLithium)
        cardPFM = findViewById(R.id.cardPFM)
        cardPMMA = findViewById(R.id.cardPMMA)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnPrevious = findViewById<TextView>(R.id.btnPrevious)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        // Set Click Listeners for Restoration
        cardCrown.setOnClickListener { selectRestoration("Crown", cardCrown) }
        cardBridge.setOnClickListener { selectRestoration("Bridge", cardBridge) }
        cardVeneer.setOnClickListener { selectRestoration("Veneer", cardVeneer) }
        cardInlayOnlay.setOnClickListener { selectRestoration("Inlay/Onlay", cardInlayOnlay) }

        // Set Click Listeners for Material
        cardZirconia.setOnClickListener { selectMaterial("Zirconia", cardZirconia) }
        cardLithium.setOnClickListener { selectMaterial("Lithium Disilicate", cardLithium) }
        cardPFM.setOnClickListener { selectMaterial("PFM", cardPFM) }
        cardPMMA.setOnClickListener { selectMaterial("PMMA", cardPMMA) }


        btnBack.setOnClickListener { finish() }
        btnPrevious.setOnClickListener { finish() }
        btnContinue.setOnClickListener {
            val condition = if (conditionSpinner.selectedItemPosition > 0) conditionSpinner.selectedItem.toString() else ""
            val toothNumbers = etToothNumbers.text.toString().trim()

            if (condition.isEmpty() || toothNumbers.isEmpty()) {
                Toast.makeText(this, "Please select clinical condition and enter tooth numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DentistNewCase4Activity::class.java).apply {
                putExtras(getIntent()) // Forward extras from Step 1
                putExtra("EXTRA_CONDITION", condition)
                putExtra("EXTRA_TOOTH_NUMBERS", toothNumbers)
                putExtra("EXTRA_RESTORATION", selectedRestoration)
                putExtra("EXTRA_MATERIAL", selectedMaterial)
            }
            startActivity(intent)
        }

        setupAiSuggestions()
    }

    private fun setupAiSuggestions() {
        conditionSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) return
                val input = parent?.getItemAtPosition(position).toString().lowercase()
                when {
                    input.contains("hypodontia") || input.contains("missing") -> {
                        selectRestoration("Bridge", cardBridge, isAiSuggested = true)
                        selectMaterial("Zirconia", cardZirconia, isAiSuggested = true)
                    }
                    input.contains("caries") -> {
                        selectRestoration("Crown", cardCrown, isAiSuggested = true)
                        selectMaterial("PFM", cardPFM, isAiSuggested = true)
                    }
                    input.contains("discoloration") -> {
                        selectRestoration("Veneer", cardVeneer, isAiSuggested = true)
                        selectMaterial("Lithium Disilicate", cardLithium, isAiSuggested = true)
                    }
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun selectRestoration(type: String, selectedCard: LinearLayout, isAiSuggested: Boolean = false) {
        selectedRestoration = type
        
        // Reset all restoration cards
        cardCrown.isSelected = false
        cardBridge.isSelected = false
        cardVeneer.isSelected = false
        cardInlayOnlay.isSelected = false

        // Select the clicked one
        selectedCard.isSelected = true
        
        if (isAiSuggested) {
            // Optional: You could show a small "AI Suggested" toast or indicator here
        }
    }

    private fun selectMaterial(material: String, selectedCard: LinearLayout, isAiSuggested: Boolean = false) {
        selectedMaterial = material
        
        // Reset all material cards
        cardZirconia.isSelected = false
        cardLithium.isSelected = false
        cardPFM.isSelected = false
        cardPMMA.isSelected = false

        // Select the clicked one
        selectedCard.isSelected = true
    }
}
