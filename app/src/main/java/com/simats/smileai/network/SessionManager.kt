package com.simats.smileai.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("SmileAI", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString("access_token", token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("access_token", null)
    }
}
