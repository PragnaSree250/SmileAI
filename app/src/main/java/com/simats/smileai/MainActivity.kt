package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to SmartLoginActivity as entry point
        startActivity(Intent(this, SmartLoginActivity::class.java))
        finish()
    }
}
