package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to RoleSelectionActivity as entry point
        startActivity(Intent(this, RoleSelectionActivity::class.java))
        finish()
    }
}
