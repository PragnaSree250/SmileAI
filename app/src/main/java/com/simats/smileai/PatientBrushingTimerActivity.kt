package com.simats.smileai

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PatientBrushingTimerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var timerProgressBar: ProgressBar
    private lateinit var btnStartStop: Button
    private lateinit var btnReset: Button
    private lateinit var tvBrushingTip: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvQuadrantLabel: TextView
    private lateinit var viewQuads: List<android.view.View>
    
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 120000 // 2 minutes
    private var lastVibratedSecond: Int = -1

    private val tips = listOf(
        "Start with the outer surfaces of your top right teeth.",
        "Now move to the top left teeth, brushing thoroughly.",
        "Time for the bottom right! Brush in circular motions.",
        "Finally, focus on the bottom left and your tongue."
    )

    private val quadLabels = listOf("UPPER RIGHT", "UPPER LEFT", "LOWER RIGHT", "LOWER LEFT")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_brushing_timer)

        tvTimer = findViewById(R.id.tvTimer)
        timerProgressBar = findViewById(R.id.timerProgressBar)
        btnStartStop = findViewById(R.id.btnStartStop)
        btnReset = findViewById(R.id.btnReset)
        tvBrushingTip = findViewById(R.id.tvBrushingTip)
        tvStreak = findViewById(R.id.tvStreak)
        tvQuadrantLabel = findViewById(R.id.tvQuadrantLabel)
        
        viewQuads = listOf(
            findViewById(R.id.viewQuad1),
            findViewById(R.id.viewQuad2),
            findViewById(R.id.viewQuad3),
            findViewById(R.id.viewQuad4)
        )

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnStartStop.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        updateStreakDisplay()
        updateCountDownText()
        updateQuadrantVisual(0)
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val totalSecondsElapsed = 120 - (timeLeftInMillis / 1000).toInt()
                
                updateCountDownText()
                updateProgressBar()
                updateTipAndQuadrant(totalSecondsElapsed)
                checkHapticFeedback(totalSecondsElapsed)
            }

            override fun onFinish() {
                isTimerRunning = false
                btnStartStop.text = "Start Brushing"
                tvBrushingTip.text = "Great job! Your smile is glowing."
                incrementStreak()
                notifyCompletion()
            }
        }.start()

        isTimerRunning = true
        btnStartStop.text = "Pause"
        tvQuadrantLabel.visibility = android.view.View.VISIBLE
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStartStop.text = "Resume"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = 120000
        updateCountDownText()
        timerProgressBar.progress = 120
        btnStartStop.text = "Start Brushing"
        tvBrushingTip.text = tips[0]
        updateQuadrantVisual(0)
        tvQuadrantLabel.visibility = android.view.View.INVISIBLE
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        tvTimer.text = timeLeftFormatted
    }

    private fun updateProgressBar() {
        val secondsLeft = (timeLeftInMillis / 1000).toInt()
        timerProgressBar.progress = secondsLeft
    }

    private fun updateTipAndQuadrant(secondsElapsed: Int) {
        val index = (secondsElapsed / 30).coerceIn(0, 3)
        tvBrushingTip.text = tips[index]
        tvQuadrantLabel.text = quadLabels[index]
        updateQuadrantVisual(index)
    }

    private fun updateQuadrantVisual(activeIndex: Int) {
        viewQuads.forEachIndexed { index, view ->
            if (index == activeIndex) {
                view.setBackgroundColor(android.graphics.Color.parseColor("#38bdf8"))
                view.alpha = 1.0f
            } else {
                view.setBackgroundResource(R.drawable.bg_card_semi_transparent)
                view.alpha = 0.3f
            }
        }
    }

    private fun checkHapticFeedback(secondsElapsed: Int) {
        // Vibrate every 30 seconds when quadrant changes
        if (secondsElapsed > 0 && secondsElapsed % 30 == 0 && secondsElapsed != lastVibratedSecond) {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(500)
            }
            lastVibratedSecond = secondsElapsed
        }
    }

    private fun updateStreakDisplay() {
        val prefs = getSharedPreferences("BrushingPrefs", android.content.Context.MODE_PRIVATE)
        val streak = prefs.getInt("brushing_streak", 0)
        tvStreak.text = "$streak Day Streak!"
    }

    private fun incrementStreak() {
        val prefs = getSharedPreferences("BrushingPrefs", android.content.Context.MODE_PRIVATE)
        val lastDate = prefs.getLong("last_brushing_date", 0)
        val today = System.currentTimeMillis()
        
        // Simple day calculation
        val oneDayMillis = 24 * 60 * 60 * 1000
        val isConsecutive = (today - lastDate) < (oneDayMillis * 2) && (today - lastDate) > oneDayMillis
        val isSameDay = android.text.format.DateUtils.isToday(lastDate)

        if (!isSameDay) {
            var streak = prefs.getInt("brushing_streak", 0)
            if (isConsecutive) {
                streak++
            } else {
                streak = 1
            }
            prefs.edit().putInt("brushing_streak", streak).putLong("last_brushing_date", today).apply()
            updateStreakDisplay()
        }
    }

    private fun notifyCompletion() {
        android.widget.Toast.makeText(this, "Brushing session complete! Streak updated ✨", android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
