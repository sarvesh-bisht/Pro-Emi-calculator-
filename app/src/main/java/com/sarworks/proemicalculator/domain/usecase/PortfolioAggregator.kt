package com.sarworks.proemicalculator.domain.usecase

import com.sarworks.proemicalculator.domain.model.LoanProfile

data class PortfolioSummary(
    val loanCount: Int,
    val totalMonthlyEmi: Double,
    val totalOutstanding: Double,
    val totalRemainingInterest: Double,
    val aggregatePayoffMonths: Int
)

class PortfolioAggregator(
    private val calculateEmiUseCase: CalculateEmiUseCase = CalculateEmiUseCase()
) {
    fun aggregate(loans: List<LoanProfile>): PortfolioSummary {
        var totalEmi = 0.0
        var totalOutstanding = 0.0
        var totalRemainingInterest = 0.0
        var maxPayoff = 0

        loans.forEach { profile ->
            val result = calculateEmiUseCase(profile.inputs)
            if (result.schedule.isNotEmpty()) {
                totalEmi += result.standardEmi
                val paidPrincipal = result.schedule.sumOf { it.principalPaid + it.extraEmi + it.partPayment }
                totalOutstanding += (profile.inputs.principal - paidPrincipal).coerceAtLeast(0.0)
                totalRemainingInterest += result.totalInterest
                maxPayoff = maxOf(maxPayoff, result.newTenureMonths)
            }
        }

        return PortfolioSummary(
            loanCount = loans.size,
            totalMonthlyEmi = totalEmi,
            totalOutstanding = totalOutstanding,
            totalRemainingInterest = totalRemainingInterest,
            aggregatePayoffMonths = maxPayoff
        )
    }
}