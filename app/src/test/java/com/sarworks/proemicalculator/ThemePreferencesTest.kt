package com.sarworks.proemicalculator

import com.sarworks.proemicalculator.data.preferences.ThemePreferences
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreferencesTest {

    @Test
    fun parseThemeKnownValue() {
        assertEquals(EmiContract.AppTheme.CRIMSON, ThemePreferences.parseTheme("CRIMSON"))
    }

    @Test
    fun parseThemeUnknownFallsBack() {
        assertEquals(EmiContract.AppTheme.DEEP_BLUE, ThemePreferences.parseTheme("NOPE"))
        assertEquals(EmiContract.AppTheme.DEEP_BLUE, ThemePreferences.parseTheme(null))
    }

    @Test
    fun parseModeKnownValue() {
        assertEquals(EmiContract.ThemeMode.DARK, ThemePreferences.parseMode("DARK"))
    }

    @Test
    fun parseModeUnknownFallsBack() {
        assertEquals(EmiContract.ThemeMode.SYSTEM, ThemePreferences.parseMode("NOPE"))
        assertEquals(EmiContract.ThemeMode.SYSTEM, ThemePreferences.parseMode(null))
    }
}