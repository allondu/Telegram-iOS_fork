package com.courier.messenger.telegram

import android.content.Context
import com.courier.messenger.BuildConfig

data class ApiCredentials(val apiId: Int, val apiHash: String)

object AppPrefs {
    private const val FILE = "courier_prefs"
    private const val KEY_ID = "api_id"
    private const val KEY_HASH = "api_hash"

    fun load(context: Context): ApiCredentials? {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val storedId = prefs.getString(KEY_ID, null)
        val storedHash = prefs.getString(KEY_HASH, null)
        if (storedId != null && storedHash != null) {
            storedId.toIntOrNull()?.let { return ApiCredentials(it, storedHash) }
        }
        val builtIn = BuildConfig.COURIER_API_ID.toIntOrNull()
        return if (builtIn != null && BuildConfig.COURIER_API_HASH.isNotBlank()) {
            ApiCredentials(builtIn, BuildConfig.COURIER_API_HASH)
        } else {
            null
        }
    }

    fun save(context: Context, apiId: Int, apiHash: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ID, apiId.toString())
            .putString(KEY_HASH, apiHash)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
