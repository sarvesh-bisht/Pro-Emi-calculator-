package com.sarworks.proemicalculator.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.sarworks.proemicalculator.domain.model.LoanProfile

class LoanProfileStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<LoanProfile> {
        val json = prefs.getString(KEY_LOANS, null) ?: return emptyList()
        return LoanProfile.listFromJsonArray(json)
    }

    fun save(profile: LoanProfile) {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
        } else {
            current.add(profile)
        }
        prefs.edit().putString(KEY_LOANS, LoanProfile.listToJsonArray(current)).apply()
    }

    fun delete(id: String) {
        val current = getAll().filterNot { it.id == id }
        prefs.edit().putString(KEY_LOANS, LoanProfile.listToJsonArray(current)).apply()
    }

    fun getById(id: String): LoanProfile? = getAll().firstOrNull { it.id == id }

    fun clear() {
        prefs.edit().remove(KEY_LOANS).apply()
    }

    companion object {
        const val PREFS_NAME = "loan_portfolio"
        const val KEY_LOANS = "loans"
    }
}