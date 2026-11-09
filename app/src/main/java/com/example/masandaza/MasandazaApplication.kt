package com.example.masandaza

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MasandazaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
