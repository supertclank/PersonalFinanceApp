package com.example.personfinanceapp

import android.app.Application
import android.content.Context
import androidx.appcompat.widget.ThemeUtils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply saved theme globally when the app starts
        ThemeUtils.applySavedTheme(this)
    }
}
