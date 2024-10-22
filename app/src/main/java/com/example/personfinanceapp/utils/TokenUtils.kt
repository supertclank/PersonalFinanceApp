package com.example.personfinanceapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject

object TokenUtils {

    private const val PREFS_NAME = "prefs"
    private const val TOKEN_KEY = "token"

    // Get the token from SharedPreferences
    fun getTokenFromStorage(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Decode the JWT token to extract claims
    fun decodeToken(token: String): Map<String, Any>? {
        val parts = token.split(".")
        if (parts.size != 3) return null

        // Decode the payload (the second part)
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val jsonString = String(decodedBytes)

        // Convert to Map
        return JSONObject(jsonString).toMap()
    }

    // Helper function to convert JSONObject to Map
    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = this.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = this.get(key)
        }
        return map
    }

    fun saveTokenToStorage(context: Context, token: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

}
