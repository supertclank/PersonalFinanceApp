package com.example.personfinanceapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
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
    internal fun decodeTokenManually(token: String): Int? {
        return try {
            // JWT is in the format: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("AppLog", "Invalid token format")
                return null
            }

            // The payload is the second part (index 1) and is Base64 encoded
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            Log.d("AppLog", "Decoded JWT Payload: $payload")

            // Convert the payload into a JSONObject
            val jsonPayload = JSONObject(payload)

            // Extract the "id" field from the payload
            val userId = jsonPayload.getInt("id")
            Log.d("AppLog", "Extracted User ID: $userId")
            userId
        } catch (e: Exception) {
            Log.e("AppLog", "Failed to decode token: ${e.message}", e)
            null
        }
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
