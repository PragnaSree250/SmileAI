package com.simats.smileai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.smileai.network.RetrofitClient

class PatientNotificationsActivity : AppCompatActivity() {
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

        // Ensure RetrofitClient is initialized
        if (RetrofitClient.authToken == null) {
            RetrofitClient.authToken = accessToken
        }

        RetrofitClient.instance.getNotifications().enqueue(object : retrofit2.Callback<List<com.simats.smileai.network.Notification>> {
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
        val container = findViewById<LinearLayout>(R.id.notificationsContainer)
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (notifications.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No new notifications"
                setTextColor(android.graphics.Color.WHITE)
                android.view.Gravity.CENTER
                setPadding(0, 50, 0, 0)
            }
            container.addView(emptyTv)
            return
        }

        for (notif in notifications) {
            val itemView = inflater.inflate(R.layout.item_patient_notification, container, false)
            itemView.findViewById<TextView>(R.id.tvNotifTitle).text = notif.title
            itemView.findViewById<TextView>(R.id.tvNotifMessage).text = notif.message
            itemView.findViewById<TextView>(R.id.tvNotifTime).text = notif.created_at.take(16).replace("T", " ")
            
            if (notif.is_read) {
                itemView.findViewById<View>(R.id.notifDot).visibility = View.GONE
            }

            container.addView(itemView)
        }
    }
}
