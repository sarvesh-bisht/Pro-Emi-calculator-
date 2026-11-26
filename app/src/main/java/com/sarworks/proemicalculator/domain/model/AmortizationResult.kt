package com.sarworks.proemicalculator.domain.model

data class MonthlyPaymentRow(
    val month: Int,
    val openingBalance: Double,
    val emiComponent: Double,
    val interestPaid: Double,
    val principalPaid: Double,
    val extraEmi: Double,
    val partPayment: Double,
    val totalPayment: Double,
    val closingBalance: Double
)

data class AmortizationResult(
    val standardEmi: Double,
    val totalPrincipal: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val originalTenureMonths: Int,
    val newTenureMonths: Int,
    val interestSaved: Double,
    val monthsSaved: Int,
    val schedule: List<MonthlyPaymentRow>
)
