package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView

class DentistSmileLibraryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_smile_library)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val cardOvoid = findViewById<CardView>(R.id.cardOvoid)
        val cardSquare = findViewById<CardView>(R.id.cardSquare)
        val cardTapering = findViewById<CardView>(R.id.cardTapering)
        val cardSquareTaper = findViewById<CardView>(R.id.cardSquareTaper)

        cardOvoid.setOnClickListener { selectTemplate("Ovoid-Tapering Hybrid") }
        cardSquare.setOnClickListener { selectTemplate("Square-Mature") }
        cardTapering.setOnClickListener { selectTemplate("Tapering-Youthful") }
        cardSquareTaper.setOnClickListener { selectTemplate("Natural-Balanced") }
    }

    private fun selectTemplate(shape: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("SELECTED_SMILE_SHAPE", shape)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
