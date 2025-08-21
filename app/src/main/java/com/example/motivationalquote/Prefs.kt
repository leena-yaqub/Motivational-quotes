package com.example.motivationalquote

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_SKIP_AUTH = "skip_auth"
    private const val KEY_USER_LOGGED_IN = "user_logged_in"
    private const val KEY_USER_SIGNED_OUT = "user_signed_out"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Add the missing get() method
    fun get(context: Context): SharedPreferences {
        return getPrefs(context)
    }

    fun shouldSkipAuth(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SKIP_AUTH, false)
    }

    fun setSkipAuth(context: Context, skip: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SKIP_AUTH, skip).apply()
    }

    fun setUserLoggedIn(context: Context, loggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_USER_LOGGED_IN, loggedIn).apply()
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_USER_LOGGED_IN, false)
    }

    fun setUserSignedOut(context: Context, signedOut: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_USER_SIGNED_OUT, signedOut).apply()
    }

    fun isUserSignedOut(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_USER_SIGNED_OUT, false)
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    // Add missing methods that are called in LoginActivity
    fun markLoggedIn(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_USER_LOGGED_IN, true)
            .putBoolean(KEY_USER_SIGNED_OUT, false)
            .putBoolean(KEY_SKIP_AUTH, true)
            .apply()
    }

    // Add missing methods that are called in ProfileActivity
    fun markSignedOut(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_USER_LOGGED_IN, false)
            .putBoolean(KEY_USER_SIGNED_OUT, true)
            .putBoolean(KEY_SKIP_AUTH, false)
            .apply()
    }
}