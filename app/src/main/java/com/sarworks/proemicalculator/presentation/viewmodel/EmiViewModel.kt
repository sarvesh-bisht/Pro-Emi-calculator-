package com.sarworks.proemicalculator.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sarworks.proemicalculator.data.preferences.ThemePreferences
import com.sarworks.proemicalculator.data.storage.LoanProfileStore
import com.sarworks.proemicalculator.data.storage.ProStatusStore
import com.sarworks.proemicalculator.domain.model.LoanInputs
import com.sarworks.proemicalculator.domain.model.LoanProfile
import com.sarworks.proemicalculator.domain.model.PartPayment
import com.sarworks.proemicalculator.domain.usecase.CalculateEmiUseCase
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class EmiViewModel @JvmOverloads constructor(
    application: Application,
    private val calculateEmiUseCase: CalculateEmiUseCase = CalculateEmiUseCase()
) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)
    private val proStatusStore = ProStatusStore(application)
    private val loanProfileStore = LoanProfileStore(application)

    private val _state = MutableStateFlow(EmiContract.State())
    val state: StateFlow<EmiContract.State> = _state.asStateFlow()

    init {
        _state.value = EmiContract.State(
            selectedTheme = themePreferences.getTheme(),
            themeMode = themePreferences.getMode(),
            isProUnlocked = proStatusStore.isUnlocked(),
            loanProfiles = loanProfileStore.getAll()
        )
        performCalculation()
    }

    fun handleIntent(intent: EmiContract.Intent) {
        when (intent) {
            is EmiContract.Intent.UpdateAmount -> _state.update { it.copy(inputs = it.inputs.copy(principal = intent.amount)) }
            is EmiContract.Intent.UpdateRate -> _state.update { it.copy(inputs = it.inputs.copy(annualRate = intent.rate)) }
            is EmiContract.Intent.UpdateDuration -> _state.update { it.copy(inputs = it.inputs.copy(duration = intent.duration)) }
            is EmiContract.Intent.UpdateDurationType -> _state.update { it.copy(inputs = it.inputs.copy(durationType = intent.type)) }
            is EmiContract.Intent.UpdateExtraEmi -> _state.update { it.copy(inputs = it.inputs.copy(extraEmi = intent.extraEmi)) }
            is EmiContract.Intent.ToggleTenureFormat -> _state.update { it.copy(tenureDisplayFormat = intent.format) }

            is EmiContract.Intent.AddPartPaymentRow -> {
                val newPP = PartPayment(UUID.randomUUID().toString(), 0, 0.0)
                _state.update { it.copy(inputs = it.inputs.copy(partPayments = it.inputs.partPayments + newPP)) }
            }
            is EmiContract.Intent.UpdatePartPayment -> {
                _state.update { state ->
                    val updatedList = state.inputs.partPayments.map {
                        if (it.id == intent.id) it.copy(month = intent.month, amount = intent.amount) else it
                    }
                    state.copy(inputs = state.inputs.copy(partPayments = updatedList))
                }
            }
            is EmiContract.Intent.RemovePartPayment -> {
                val updatedList = _state.value.inputs.partPayments.filterNot { it.id == intent.id }
                _state.update { it.copy(inputs = it.inputs.copy(partPayments = updatedList)) }
            }
            EmiContract.Intent.Reset -> {
                val currentTheme = _state.value.selectedTheme
                val currentMode = _state.value.themeMode
                val isPro = _state.value.isProUnlocked
                _state.value = EmiContract.State(selectedTheme = currentTheme, themeMode = currentMode, isProUnlocked = isPro)
                performCalculation()
            }
            is EmiContract.Intent.UpdateTheme -> {
                themePreferences.setTheme(intent.theme)
                _state.update { it.copy(selectedTheme = intent.theme) }
            }
            is EmiContract.Intent.UpdateThemeMode -> {
                themePreferences.setMode(intent.mode)
                _state.update { it.copy(themeMode = intent.mode) }
            }
            is EmiContract.Intent.UpdateProStatus -> {
                proStatusStore.setUnlocked(intent.unlocked)
                _state.update { it.copy(isProUnlocked = intent.unlocked) }
            }
            is EmiContract.Intent.StartEditingLoan -> {
                val profile = intent.loanId?.let { loanProfileStore.getById(it) }
                if (profile != null) {
                    _state.update { it.copy(inputs = profile.inputs, editingLoanId = profile.id, isLoanEditingMode = true) }
                } else {
                    _state.update { it.copy(inputs = LoanInputs(), editingLoanId = null, isLoanEditingMode = true) }
                }
                performCalculation()
            }
            EmiContract.Intent.EnterLoanEditingMode -> {
                _state.update { it.copy(inputs = LoanInputs(), editingLoanId = null, isLoanEditingMode = true) }
                performCalculation()
            }
            EmiContract.Intent.ExitLoanEditingMode -> {
                _state.update { it.copy(isLoanEditingMode = false, editingLoanId = null, inputs = LoanInputs()) }
                performCalculation()
            }
            is EmiContract.Intent.SaveLoanProfile -> {
                val profile = LoanProfile(
                    id = _state.value.editingLoanId ?: UUID.randomUUID().toString(),
                    name = intent.name.ifBlank { "Untitled Loan" },
                    lender = intent.lender,
                    inputs = _state.value.inputs
                )
                loanProfileStore.save(profile)
                _state.update { it.copy(editingLoanId = null, isLoanEditingMode = false, loanProfiles = loanProfileStore.getAll()) }
            }
            EmiContract.Intent.CancelEditingLoan -> {
                _state.update { it.copy(editingLoanId = null, isLoanEditingMode = false, inputs = LoanInputs()) }
                performCalculation()
            }
            EmiContract.Intent.Calculate -> performCalculation()
        }

        if (intent !is EmiContract.Intent.Calculate &&
            intent !is EmiContract.Intent.StartEditingLoan &&
            intent !is EmiContract.Intent.SaveLoanProfile &&
            intent !is EmiContract.Intent.CancelEditingLoan
        ) {
            performCalculation()
        }
    }

    fun outstandingBalanceBefore(month: Int): Double =
        calculateEmiUseCase.outstandingBalanceBefore(month, _state.value.inputs)

    fun invalidPartPaymentIds(): Set<String> =
        calculateEmiUseCase.invalidPartPaymentIds(_state.value.inputs)

    fun loanProfileStore(): LoanProfileStore = loanProfileStore

    fun getAllLoanProfiles(): List<LoanProfile> = loanProfileStore.getAll()

    fun deleteLoanProfile(id: String) {
        loanProfileStore.delete(id)
        _state.update { it.copy(loanProfiles = loanProfileStore.getAll()) }
    }

    private fun performCalculation() {
        val result = calculateEmiUseCase(_state.value.inputs)
        _state.update { it.copy(result = result) }
    }
}