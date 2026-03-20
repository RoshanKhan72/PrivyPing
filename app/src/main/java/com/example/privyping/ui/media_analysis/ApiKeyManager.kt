package com.example.privyping.ui.media_analysis

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ApiKeyManager {

    private const val PREF_NAME = "secure_api_prefs"
    private const val KEY_GEMINI = "gemini_api_key"

    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveGeminiApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI, key.trim()).apply()
    }

    fun getGeminiApiKey(context: Context): String? {
        return getPrefs(context).getString(KEY_GEMINI, null)
    }

    fun hasApiKey(context: Context): Boolean {
        return !getGeminiApiKey(context).isNullOrBlank()
    }

    fun clearApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_GEMINI).apply()
    }
}
