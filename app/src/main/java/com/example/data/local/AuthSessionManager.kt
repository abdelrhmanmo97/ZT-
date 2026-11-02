package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AuthSessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "salary_auth_session"
        private const val KEY_REMEMBER_ME = "key_remember_me"
        private const val KEY_LOGGED_IN_EMAIL = "key_logged_in_email"
        private const val KEY_LOGGED_IN_USER_ID = "key_logged_in_user_id"
    }

    fun saveSession(email: String, userId: Long, rememberMe: Boolean) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putString(KEY_LOGGED_IN_EMAIL, email.trim().lowercase())
            .putLong(KEY_LOGGED_IN_USER_ID, userId)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, false)
            .remove(KEY_LOGGED_IN_EMAIL)
            .remove(KEY_LOGGED_IN_USER_ID)
            .apply()
    }

    fun isRemembered(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun getSavedEmail(): String? {
        return prefs.getString(KEY_LOGGED_IN_EMAIL, null)
    }

    fun getSavedUserId(): Long {
        return prefs.getLong(KEY_LOGGED_IN_USER_ID, -1L)
    }
}
