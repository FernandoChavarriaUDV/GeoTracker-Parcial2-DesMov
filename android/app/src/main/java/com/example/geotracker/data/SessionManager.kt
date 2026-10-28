package com.example.geotracker.data

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    fun token(): String? = preferences.getString("token", null)
    fun save(token: String) { preferences.edit().putString("token", token).apply() }
    fun clear() { preferences.edit().clear().apply() }
}
