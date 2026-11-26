package com.sarworks.proemicalculator.data.storage

import android.content.Context
import android.content.SharedPreferences

class ProStatusStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isUnlocked(): Boolean = prefs.getBoolean(KEY_UNLOCKED, false)

    fun setUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_UNLOCKED, unlocked).apply()
    }

    companion object {
        const val PREFS_NAME = "pro_status"
        const val KEY_UNLOCKED = "is_pro_unlocked"
    }
}