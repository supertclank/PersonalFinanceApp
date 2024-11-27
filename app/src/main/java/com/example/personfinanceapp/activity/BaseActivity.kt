package com.example.personfinanceapp.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import api.RetrofitClient
import com.example.personfinanceapp.utils.SharedPreferenceManager

open class BaseActivity : AppCompatActivity() {

    private val TAG = "BaseActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        applyUserPreferences()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called - applying user preferences")
        applyUserPreferences()
    }

    // Apply user preferences (theme and font size)
    private fun applyUserPreferences() {
        val sharedPrefManager = SharedPreferenceManager(this, apiService = RetrofitClient.instance)

        // Theme application
        val isDarkModeEnabled = sharedPrefManager.isDarkModeEnabled()
        val newNightMode = if (isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        // Only update if the mode is different
        if (AppCompatDelegate.getDefaultNightMode() != newNightMode) {
            AppCompatDelegate.setDefaultNightMode(newNightMode)
            delegate.applyDayNight() // or recreate() if needed
        }

        // Font size application
        val fontSize = sharedPrefManager.getFontSize()
        setFontScale(fontSize)
    }

    // Set font scale based on preference
    private fun setFontScale(fontSize: String?) {
        val scale = when (fontSize) {
            "Small" -> 0.85f
            "Large" -> 1.15f
            else -> 1.0f // Default or "Normal"
        }

        // Get current configuration
        val configuration = resources.configuration

        // Only update if the scale is different
        if (configuration.fontScale != scale) {
            configuration.fontScale = scale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}