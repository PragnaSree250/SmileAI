package com.simats.smileai

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PatientHelpAndSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_help_and_support)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // FAQ 1
        val faqHeader1 = findViewById<LinearLayout>(R.id.faqHeader1)
        val answer1 = findViewById<TextView>(R.id.answer1)
        val arrow1 = findViewById<ImageView>(R.id.arrow1)
        faqHeader1.setOnClickListener { toggleFAQ(answer1, arrow1) }

        // FAQ 2
        val faqHeader2 = findViewById<LinearLayout>(R.id.faqHeader2)
        val answer2 = findViewById<TextView>(R.id.answer2)
        val arrow2 = findViewById<ImageView>(R.id.arrow2)
        faqHeader2.setOnClickListener { toggleFAQ(answer2, arrow2) }

        // FAQ 3
        val faqHeader3 = findViewById<LinearLayout>(R.id.faqHeader3)
        val answer3 = findViewById<TextView>(R.id.answer3)
        val arrow3 = findViewById<ImageView>(R.id.arrow3)
        faqHeader3.setOnClickListener { toggleFAQ(answer3, arrow3) }

        // FAQ 4
        val faqHeader4 = findViewById<LinearLayout>(R.id.faqHeader4)
        val answer4 = findViewById<TextView>(R.id.answer4)
        val arrow4 = findViewById<ImageView>(R.id.arrow4)
        faqHeader4.setOnClickListener { toggleFAQ(answer4, arrow4) }
    }

    private fun toggleFAQ(answer: TextView, arrow: ImageView) {
        if (answer.visibility == View.GONE) {
            answer.visibility = View.VISIBLE
            arrow.animate().rotation(180f).setDuration(200).start()
        } else {
            answer.visibility = View.GONE
            arrow.animate().rotation(0f).setDuration(200).start()
        }
    }
}
