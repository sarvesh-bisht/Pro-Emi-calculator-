package com.sarworks.proemicalculator.presentation.mvi

import com.sarworks.proemicalculator.domain.model.*

class EmiContract {

    // 1. Immutable UI State
    data class State(
        val inputs: LoanInputs = LoanInputs(),
        val tenureDisplayFormat: TenureDisplayFormat = TenureDisplayFormat.MONTHS,
        val result: AmortizationResult = AmortizationResult(0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, emptyList()),
        val selectedTheme: AppTheme = AppTheme.DEEP_BLUE,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val isProUnlocked: Boolean = false,
        val editingLoanId: String? = null,
        val isLoanEditingMode: Boolean = false,
        val loanProfiles: List<com.sarworks.proemicalculator.domain.model.LoanProfile> = emptyList()
    )

    enum class AppTheme {
        DEEP_BLUE, EMERALD, CRIMSON, SLATE, PURPLE
    }

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    // 2. User Actions / Intents
    sealed class Intent {
        data class UpdateAmount(val amount: Double) : Intent()
        data class UpdateRate(val rate: Double) : Intent()
        data class UpdateDuration(val duration: Int) : Intent()
        data class UpdateDurationType(val type: DurationType) : Intent()
        data class UpdateExtraEmi(val extraEmi: Double) : Intent()
        data class ToggleTenureFormat(val format: TenureDisplayFormat) : Intent()
        object AddPartPaymentRow : Intent()
        data class UpdatePartPayment(val id: String, val month: Int, val amount: Double) : Intent()
        data class RemovePartPayment(val id: String) : Intent()
        object Reset : Intent()
        data class UpdateTheme(val theme: AppTheme) : Intent()
        data class UpdateThemeMode(val mode: ThemeMode) : Intent()
        data class UpdateProStatus(val unlocked: Boolean) : Intent()
        data class StartEditingLoan(val loanId: String?) : Intent()
        data class SaveLoanProfile(val name: String, val lender: String) : Intent()
        object CancelEditingLoan : Intent()
        object EnterLoanEditingMode : Intent()
        object ExitLoanEditingMode : Intent()
        object Calculate : Intent()
    }

    // 3. Side Effects (Toasts, Navigation)
    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
    }
}
