package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import android.widget.TextView
import com.simats.smileai.ml.YoloDetector
import android.graphics.BitmapFactory
import android.net.Uri

class PatientViewAiAnalysisActivity : ComponentActivity() {
    private lateinit var detector: YoloDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_view_ai_analysis)

        detector = YoloDetector(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence) // New ID
        val pbConfidence = findViewById<ProgressBar>(R.id.pbConfidence) // New ID
        val tvFinding1Title = findViewById<TextView>(R.id.tvFinding1Title) // New ID
        val tvFinding1Desc = findViewById<TextView>(R.id.tvFinding1Desc) // New ID

        // Populate with dynamic data or AI results
        val deficiency = intent.getStringExtra("EXTRA_DEFICIENCY") ?: "Structural Deficiency"
        val recommendation = intent.getStringExtra("EXTRA_RECOMMENDATION") ?: "Structural restoration advised."
        val explanation = intent.getStringExtra("EXTRA_EXPLANATION") ?: "The AI identifies a structural deficiency that requires a multi-unit approach to ensure long-term stability and bite alignment."
        val symmetry = intent.getStringExtra("EXTRA_SYMMETRY") ?: "92%"
        val goldenRatio = intent.getStringExtra("EXTRA_GOLDEN_RATIO") ?: "1.618"
        var tooth = intent.getStringExtra("EXTRA_TOOTH") ?: "3"

        // Try to perform real AI detection if an image URI is provided
        intent.getStringExtra("IMAGE_URI")?.let { uriString ->
            try {
                val inputStream = contentResolver.openInputStream(Uri.parse(uriString))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val results = detector.detect(bitmap)
                if (results.isNotEmpty()) {
                    tooth = results[0].label
                    val confidence = (results[0].confidence * 100).toInt()
                    tvConfidence.text = "$confidence%"
                    pbConfidence.progress = confidence
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        tvFinding1Title.text = "$deficiency Detected"
        tvFinding1Desc.text = "Analysis shows alignment issues on tooth #$tooth requiring clinical attention."

        // Bind Treatment Summary
        findViewById<TextView>(R.id.tvPatientRecommendation).text = recommendation
        findViewById<TextView>(R.id.tvPatientExplanation).text = explanation
        findViewById<TextView>(R.id.tvPatientSymmetry).text = symmetry
        findViewById<TextView>(R.id.tvPatientGoldenRatio).text = goldenRatio
        
        // New fields
        findViewById<TextView>(R.id.tvPatientMedications).text = intent.getStringExtra("EXTRA_MEDS") ?: "Standard oral care suggested."
        findViewById<TextView>(R.id.tvPatientCareTips).text = intent.getStringExtra("EXTRA_CARE") ?: "Maintain standard 2x daily brushing and flossing."

        btnBack.setOnClickListener {
            finish()
        }

        // Bottom Navigation
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navReports = findViewById<LinearLayout>(R.id.navReports)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        navHome.setOnClickListener {
             startActivity(Intent(this, PatientDashboardActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        navCases.setOnClickListener {
             startActivity(Intent(this, PatientCaseAllActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }

        navReports.setOnClickListener {
             startActivity(Intent(this, PatientReportActivity::class.java))
             overridePendingTransition(0, 0)
             finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }
}
