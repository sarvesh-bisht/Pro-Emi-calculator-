package com.sarworks.proemicalculator.domain.usecase

import com.sarworks.proemicalculator.domain.model.*
import kotlin.math.pow

class CalculateEmiUseCase {

    operator fun invoke(inputs: LoanInputs): AmortizationResult {
        val totalMonths = if (inputs.durationType == DurationType.YEARS) inputs.duration * 12 else inputs.duration
        if (inputs.principal <= 0 || inputs.annualRate <= 0 || totalMonths <= 0) {
            return emptyResult()
        }

        val monthlyRate = inputs.annualRate / 12 / 100
        val baseEmi = calculateEmi(inputs.principal, monthlyRate, totalMonths)

        // Only valid part payments affect the schedule.
        val validPayments = validPartPayments(inputs, totalMonths)

        // 1. Calculate Base Trajectory (No Extra EMI / Part Payments)
        val baseSchedule = runSimulation(inputs.principal, monthlyRate, totalMonths, baseEmi, 0.0, emptyList())
        val baseTotalInterest = baseSchedule.sumOf { it.interestPaid }

        // 2. Calculate Actual Trajectory
        val actualSchedule = runSimulation(inputs.principal, monthlyRate, totalMonths, baseEmi, inputs.extraEmi, validPayments)
        val actualTotalInterest = actualSchedule.sumOf { it.interestPaid }
        val actualTotalPrincipal = actualSchedule.sumOf { it.principalPaid + it.extraEmi + it.partPayment }

        val interestSaved = (baseTotalInterest - actualTotalInterest).coerceAtLeast(0.0)
        val monthsSaved = (totalMonths - actualSchedule.size).coerceAtLeast(0)

        return AmortizationResult(
            standardEmi = baseEmi,
            totalPrincipal = actualTotalPrincipal,
            totalInterest = actualTotalInterest,
            totalPayment = actualTotalPrincipal + actualTotalInterest,
            originalTenureMonths = totalMonths,
            newTenureMonths = actualSchedule.size,
            interestSaved = interestSaved,
            monthsSaved = monthsSaved,
            schedule = actualSchedule
        )
    }

    /**
     * Outstanding principal immediately before [month], replaying months 1..month-1
     * with regular EMI principal reduction, extra EMI, and earlier (valid) part payments.
     */
    fun outstandingBalanceBefore(month: Int, inputs: LoanInputs): Double {
        val totalMonths = if (inputs.durationType == DurationType.YEARS) inputs.duration * 12 else inputs.duration
        if (inputs.principal <= 0 || inputs.annualRate <= 0 || totalMonths <= 0) return 0.0
        if (month <= 1) return inputs.principal

        val monthlyRate = inputs.annualRate / 12 / 100
        val baseEmi = calculateEmi(inputs.principal, monthlyRate, totalMonths)
        val validAggregates = validAggregatesByMonth(inputs, totalMonths)

        var balance = inputs.principal
        for (m in 1 until month) {
            if (balance <= 0.01) break
            val opening = balance
            val interest = opening * monthlyRate
            val emiComp = if (opening + interest < baseEmi) opening + interest else baseEmi
            val principalComp = emiComp - interest
            val currentExtra = inputs.extraEmi
            val currentPart = validAggregates[m] ?: 0.0

            var part = currentPart
            var extra = currentExtra
            var tp = principalComp + extra + part
            if (tp > opening) {
                val overpay = tp - opening
                tp = opening
                if (part > overpay) {
                    part -= overpay
                } else {
                    val remaining = overpay - part
                    part = 0.0
                    extra = (extra - remaining).coerceAtLeast(0.0)
                }
            }
            balance = (opening - tp).coerceAtLeast(0.0)
        }
        return balance
    }

    /**
     * Returns the set of part-payment ids that are valid under the agreed rules.
     */
    fun invalidPartPaymentIds(inputs: LoanInputs): Set<String> {
        val totalMonths = if (inputs.durationType == DurationType.YEARS) inputs.duration * 12 else inputs.duration
        if (inputs.principal <= 0 || inputs.annualRate <= 0 || totalMonths <= 0) {
            return inputs.partPayments.map { it.id }.toSet()
        }
        val validIds = validPartPaymentIds(inputs, totalMonths)
        return inputs.partPayments.map { it.id }.toSet() - validIds
    }

    private fun validPartPaymentIds(inputs: LoanInputs, totalMonths: Int): Set<String> {
        val grouped = inputs.partPayments.groupBy { it.month }
        val validIds = mutableSetOf<String>()
        val validAggregates = mutableMapOf<Int, Double>()
        for (m in grouped.keys.sorted()) {
            val group = grouped[m] ?: continue
            val aggregate = group.sumOf { it.amount }
            val monthValid = m in 1..totalMonths
            val amountValid = aggregate > 0
            val balanceBefore = balanceBeforeWithAggregates(inputs, totalMonths, validAggregates, m)
            val withinBalance = aggregate <= balanceBefore + 1e-9
            if (monthValid && amountValid && withinBalance) {
                group.forEach { validIds += it.id }
                validAggregates[m] = aggregate
            }
        }
        return validIds
    }

    private fun validPartPayments(inputs: LoanInputs, totalMonths: Int): List<PartPayment> {
        val validIds = validPartPaymentIds(inputs, totalMonths)
        return inputs.partPayments.filter { it.id in validIds }
    }

    private fun validAggregatesByMonth(inputs: LoanInputs, totalMonths: Int): Map<Int, Double> {
        val grouped = inputs.partPayments.groupBy { it.month }
        val validAggregates = mutableMapOf<Int, Double>()
        val validIds = validPartPaymentIds(inputs, totalMonths)
        for (m in grouped.keys.sorted()) {
            val group = grouped[m] ?: continue
            if (group.none { it.id in validIds }) continue
            validAggregates[m] = group.sumOf { it.amount }
        }
        return validAggregates
    }

    private fun balanceBeforeWithAggregates(
        inputs: LoanInputs,
        totalMonths: Int,
        validAggregates: Map<Int, Double>,
        targetMonth: Int
    ): Double {
        if (targetMonth <= 1) return inputs.principal
        val monthlyRate = inputs.annualRate / 12 / 100
        val baseEmi = calculateEmi(inputs.principal, monthlyRate, totalMonths)
        var balance = inputs.principal
        for (m in 1 until targetMonth) {
            if (balance <= 0.01) break
            val opening = balance
            val interest = opening * monthlyRate
            val emiComp = if (opening + interest < baseEmi) opening + interest else baseEmi
            val principalComp = emiComp - interest
            val currentExtra = inputs.extraEmi
            val currentPart = validAggregates[m] ?: 0.0

            var part = currentPart
            var extra = currentExtra
            var tp = principalComp + extra + part
            if (tp > opening) {
                val overpay = tp - opening
                tp = opening
                if (part > overpay) {
                    part -= overpay
                } else {
                    val remaining = overpay - part
                    part = 0.0
                    extra = (extra - remaining).coerceAtLeast(0.0)
                }
            }
            balance = (opening - tp).coerceAtLeast(0.0)
        }
        return balance
    }

    private fun calculateEmi(p: Double, r: Double, n: Int): Double {
        val compound = (1 + r).pow(n.toDouble())
        return p * r * compound / (compound - 1)
    }

    private fun runSimulation(
        principal: Double,
        r: Double,
        maxMonths: Int,
        baseEmi: Double,
        extraEmi: Double,
        partPayments: List<PartPayment>
    ): List<MonthlyPaymentRow> {
        // Aggregate same-month payments by summing.
        val partPaymentMap = partPayments
            .filter { it.month > 0 && it.amount > 0 }
            .groupBy { it.month }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        var balance = principal
        val schedule = mutableListOf<MonthlyPaymentRow>()
        var month = 1

        while (balance > 0.01 && month <= 1200) {
            val opening = balance
            val interest = opening * r
            val emiComp = if (opening + interest < baseEmi) opening + interest else baseEmi
            val principalComp = emiComp - interest

            var currentExtra = extraEmi
            var currentPart = partPaymentMap[month] ?: 0.0

            var totalPrincipal = principalComp + currentExtra + currentPart
            if (totalPrincipal > opening) {
                val overpay = totalPrincipal - opening
                totalPrincipal = opening
                if (currentPart > overpay) {
                    currentPart -= overpay
                } else {
                    val remaining = overpay - currentPart
                    currentPart = 0.0
                    currentExtra = (currentExtra - remaining).coerceAtLeast(0.0)
                }
            }

            balance = (opening - totalPrincipal).coerceAtLeast(0.0)
            schedule.add(
                MonthlyPaymentRow(
                    month = month,
                    openingBalance = opening,
                    emiComponent = emiComp,
                    interestPaid = interest,
                    principalPaid = principalComp,
                    extraEmi = currentExtra,
                    partPayment = currentPart,
                    totalPayment = interest + totalPrincipal,
                    closingBalance = balance
                )
            )
            month++
        }
        return schedule
    }

    private fun emptyResult() = AmortizationResult(0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, emptyList())
}