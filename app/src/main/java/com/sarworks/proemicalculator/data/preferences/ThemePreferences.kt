package com.sarworks.proemicalculator.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.sarworks.proemicalculator.presentation.mvi.EmiContract

/**
 * Thin SharedPreferences wrapper for persisting theme selection and mode.
 * Unknown stored values fall back to defaults (Deep Blue / System).
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTheme(): EmiContract.AppTheme =
        parseTheme(prefs.getString(KEY_THEME, null))

    fun setTheme(theme: EmiContract.AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun getMode(): EmiContract.ThemeMode =
        parseMode(prefs.getString(KEY_MODE, null))

    fun setMode(mode: EmiContract.ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    companion object {
        const val PREFS_NAME = "theme_prefs"
        const val KEY_THEME = "selected_theme"
        const val KEY_MODE = "theme_mode"

        fun parseTheme(name: String?): EmiContract.AppTheme =
            EmiContract.AppTheme.values().firstOrNull { it.name == name }
                ?: EmiContract.AppTheme.DEEP_BLUE

        fun parseMode(name: String?): EmiContract.ThemeMode =
            EmiContract.ThemeMode.values().firstOrNull { it.name == name }
                ?: EmiContract.ThemeMode.SYSTEM
    }
}