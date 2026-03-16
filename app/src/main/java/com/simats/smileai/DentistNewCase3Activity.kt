package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.ComponentActivity

class DentistNewCase3Activity : ComponentActivity() {

    private var selectedRestoration: String? = null
    private var selectedMaterial: String? = null

    private lateinit var etCondition: EditText
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

        etCondition = findViewById(R.id.etCondition)
        etToothNumbers = findViewById(R.id.etToothNumbers)

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
            val condition = etCondition.text.toString().trim()
            val toothNumbers = etToothNumbers.text.toString().trim()

            if (condition.isEmpty() || toothNumbers.isEmpty()) {
                Toast.makeText(this, "Please enter clinical condition and tooth numbers", Toast.LENGTH_SHORT).show()
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
        etCondition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().lowercase()
                when {
                    input.contains("missing") || input.contains("gap") || input.contains("hypodontia") -> {
                        selectRestoration("Bridge", cardBridge, isAiSuggested = true)
                        selectMaterial("Zirconia", cardZirconia, isAiSuggested = true)
                    }
                    input.contains("caries") || input.contains("cavity") || input.contains("decay") -> {
                        selectRestoration("Crown", cardCrown, isAiSuggested = true)
                        selectMaterial("PFM", cardPFM, isAiSuggested = true)
                    }
                    input.contains("discolor") || input.contains("stain") || input.contains("yellow") -> {
                        selectRestoration("Veneer", cardVeneer, isAiSuggested = true)
                        selectMaterial("Lithium Disilicate", cardLithium, isAiSuggested = true)
                    }
                    input.contains("fracture") || input.contains("chip") || input.contains("broken") -> {
                        selectRestoration("Inlay/Onlay", cardInlayOnlay, isAiSuggested = true)
                        selectMaterial("Zirconia", cardZirconia, isAiSuggested = true)
                    }
                }
            }
        })
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
