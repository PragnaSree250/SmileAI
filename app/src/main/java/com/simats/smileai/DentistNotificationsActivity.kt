package com.simats.smileai

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class DentistNotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_notifications)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnMarkAllRead = findViewById<TextView>(R.id.btnMarkAllRead)

        btnBack.setOnClickListener {
            finish()
        }

        btnMarkAllRead.setOnClickListener {
            Toast.makeText(this, "Marked all as read", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@DentistNotificationsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUi(notifications: List<com.simats.smileai.network.Notification>) {
        // Ideally update a RecyclerView. For now, just show a count.
        Toast.makeText(this, "Loaded ${notifications.size} alerts", Toast.LENGTH_SHORT).show()
    }
}
