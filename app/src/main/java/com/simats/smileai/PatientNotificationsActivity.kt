package com.simats.smileai

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class PatientNotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_notifications)

        // Header Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btnMarkRead).setOnClickListener {
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show()
        }

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) return

        com.simats.smileai.network.RetrofitClient.instance.getNotifications().enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Notification>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.smileai.network.Notification>>, response: retrofit2.Response<List<com.simats.smileai.network.Notification>>) {
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    updateUi(notifications)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<com.simats.smileai.network.Notification>>, t: Throwable) {
                Toast.makeText(this@PatientNotificationsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUi(notifications: List<com.simats.smileai.network.Notification>) {
        Toast.makeText(this, "Loaded ${notifications.size} notifications", Toast.LENGTH_SHORT).show()
    }
}
