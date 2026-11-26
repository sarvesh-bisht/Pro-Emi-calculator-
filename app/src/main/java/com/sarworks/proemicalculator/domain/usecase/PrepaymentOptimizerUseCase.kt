package com.sarworks.proemicalculator.domain.usecase

import com.sarworks.proemicalculator.domain.model.LoanInputs
import com.sarworks.proemicalculator.domain.model.LoanProfile
import com.sarworks.proemicalculator.domain.model.PartPayment
import java.util.UUID

data class OptimizationResult(
    val loanId: String,
    val loanName: String,
    val interestSaved: Double,
    val monthsSaved: Int,
    val newPayoffMonth: Int,
    val baselineInterest: Double,
    val optimizedInterest: Double
)

data class ScenarioResult(
    val baselineInterest: Double,
    val optimizedInterest: Double,
    val interestSaved: Double,
    val baselineMonths: Int,
    val optimizedMonths: Int,
    val monthsSaved: Int
)

class PrepaymentOptimizerUseCase(
    private val calculateEmiUseCase: CalculateEmiUseCase = CalculateEmiUseCase()
) {

    /**
     * Ranks loans by interest saved when applying [surplus] as a lump-sum
     * part payment at [targetMonth] (default 1). Returns results sorted by
     * interestSaved descending.
     */
    fun optimize(
        loans: List<LoanProfile>,
        surplus: Double,
        targetMonth: Int = 1
    ): List<OptimizationResult> {
        if (surplus <= 0) return emptyList()
        return loans.mapNotNull { profile ->
            val baseline = calculateEmiUseCase(profile.inputs)
            if (baseline.schedule.isEmpty()) return@mapNotNull null

            val modifiedInputs = profile.inputs.copy(
                partPayments = profile.inputs.partPayments + PartPayment(
                    id = "opt-${profile.id}",
                    month = targetMonth.coerceIn(1, profile.inputs.duration * (if (profile.inputs.durationType.name == "YEARS") 12 else 1)),
                    amount = surplus
                )
            )
            val optimized = calculateEmiUseCase(modifiedInputs)

            val interestSaved = (baseline.totalInterest - optimized.totalInterest).coerceAtLeast(0.0)
            val monthsSaved = (baseline.newTenureMonths - optimized.newTenureMonths).coerceAtLeast(0)

            OptimizationResult(
                loanId = profile.id,
                loanName = profile.name,
                interestSaved = interestSaved,
                monthsSaved = monthsSaved,
                newPayoffMonth = optimized.newTenureMonths,
                baselineInterest = baseline.totalInterest,
                optimizedInterest = optimized.totalInterest
            )
        }.sortedByDescending { it.interestSaved }
    }

    /**
     * Simulates adding [extraMonthly] as extra EMI to a single loan.
     */
    fun simulateScenario(
        profile: LoanProfile,
        extraMonthly: Double
    ): ScenarioResult {
        val baseline = calculateEmiUseCase(profile.inputs)
        val modifiedInputs = profile.inputs.copy(extraEmi = profile.inputs.extraEmi + extraMonthly)
        val optimized = calculateEmiUseCase(modifiedInputs)

        return ScenarioResult(
            baselineInterest = baseline.totalInterest,
            optimizedInterest = optimized.totalInterest,
            interestSaved = (baseline.totalInterest - optimized.totalInterest).coerceAtLeast(0.0),
            baselineMonths = baseline.newTenureMonths,
            optimizedMonths = optimized.newTenureMonths,
            monthsSaved = (baseline.newTenureMonths - optimized.newTenureMonths).coerceAtLeast(0)
        )
    }
}