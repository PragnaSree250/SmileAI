package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val btnGetStarted = findViewById<android.widget.Button>(R.id.btnGetStarted)

        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, SmartLoginActivity::class.java))
            finish()
        }
    }
}
