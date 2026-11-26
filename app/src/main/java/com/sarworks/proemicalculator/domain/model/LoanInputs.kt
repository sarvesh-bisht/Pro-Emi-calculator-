package com.sarworks.proemicalculator.domain.model

enum class DurationType { YEARS, MONTHS }
enum class TenureDisplayFormat { MONTHS, YEARS_AND_MONTHS }

data class PartPayment(
    val id: String,
    val month: Int,
    val amount: Double
)

data class LoanInputs(
    val principal: Double = 1000000.0,  // Default ₹10 Lakhs for immediate visibility
    val annualRate: Double = 8.5,
    val duration: Int = 20,
    val durationType: DurationType = DurationType.YEARS,
    val extraEmi: Double = 0.0,
    val partPayments: List<PartPayment> = emptyList()
)
