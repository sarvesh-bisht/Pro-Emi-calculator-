package com.sarworks.proemicalculator

import com.sarworks.proemicalculator.domain.model.DurationType
import com.sarworks.proemicalculator.domain.model.LoanInputs
import com.sarworks.proemicalculator.domain.model.PartPayment
import com.sarworks.proemicalculator.domain.usecase.CalculateEmiUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateEmiUseCaseTest {

    private val useCase = CalculateEmiUseCase()
    private val tol = 1e-6

    @Test
    fun standardEmiGoldenValue() {
        val inputs = LoanInputs(principal = 1_000_000.0, annualRate = 8.5, duration = 20, durationType = DurationType.YEARS)
        val result = useCase(inputs)
        assertEquals(8678.232333655342, result.standardEmi, tol)
    }

    @Test
    fun invalidInputsReturnEmptyResult() {
        val inputs = LoanInputs(principal = 0.0, annualRate = 8.5, duration = 20, durationType = DurationType.YEARS)
        val result = useCase(inputs)
        assertEquals(0.0, result.standardEmi, tol)
        assertTrue(result.schedule.isEmpty())
    }

    @Test
    fun finalMonthEmiAdjustsToRemainingBalance() {
        val inputs = LoanInputs(principal = 100_000.0, annualRate = 10.0, duration = 12, durationType = DurationType.MONTHS)
        val result = useCase(inputs)
        val last = result.schedule.last()
        assertEquals(last.openingBalance + last.interestPaid, last.emiComponent, tol)
    }

    @Test
    fun scheduleReconcilesPrincipalAndTotals() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 12,
            durationType = DurationType.MONTHS,
            extraEmi = 500.0,
            partPayments = listOf(PartPayment("1", 3, 5_000.0), PartPayment("2", 6, 10_000.0))
        )
        val result = useCase(inputs)
        val sum = result.schedule.sumOf { it.principalPaid + it.extraEmi + it.partPayment }
        assertEquals(inputs.principal, sum, 1e-3)
        assertEquals(result.totalPrincipal + result.totalInterest, result.totalPayment, tol)
        result.schedule.forEach { row ->
            assertTrue(itprincipalOrZero(row) >= 0)
            assertTrue(row.principalPaid + row.extraEmi + row.partPayment <= row.openingBalance + 1e-9)
            assertEquals(row.interestPaid + (row.principalPaid + row.extraEmi + row.partPayment), row.totalPayment, 1e-6)
        }
    }

    private fun itprincipalOrZero(row: com.sarworks.proemicalculator.domain.model.MonthlyPaymentRow): Double = row.principalPaid

    @Test
    fun duplicateMonthPaymentsAreSummed() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 60,
            durationType = DurationType.MONTHS,
            partPayments = listOf(PartPayment("a", 12, 5_000.0), PartPayment("b", 12, 3_000.0))
        )
        val result = useCase(inputs)
        val row = result.schedule.first { it.month == 12 }
        assertEquals(8_000.0, row.partPayment, tol)
    }

    @Test
    fun overpaymentAllocationDoesNotExceedOpening() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 12,
            durationType = DurationType.MONTHS,
            partPayments = listOf(PartPayment("1", 6, 90_000.0))
        )
        val result = useCase(inputs)
        val row = result.schedule.first { it.month == 6 }
        assertTrue(row.principalPaid + row.extraEmi + row.partPayment <= row.openingBalance + 1e-9)
    }

    @Test
    fun finalMonthPartPaymentContributesZero() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 12,
            durationType = DurationType.MONTHS,
            partPayments = listOf(PartPayment("1", 12, 50_000.0))
        )
        val result = useCase(inputs)
        val last = result.schedule.last()
        assertEquals(0.0, last.partPayment, tol)
    }

    @Test
    fun outstandingBalanceBeforeMatchesScheduleOpening() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 12,
            durationType = DurationType.MONTHS,
            extraEmi = 200.0,
            partPayments = listOf(PartPayment("1", 3, 5_000.0))
        )
        val result = useCase(inputs)
        val openingMonth6 = result.schedule.first { it.month == 6 }.openingBalance
        assertEquals(openingMonth6, useCase.outstandingBalanceBefore(6, inputs), 1e-6)
    }

    @Test
    fun invalidRowsAreExcluded() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 12,
            durationType = DurationType.MONTHS,
            partPayments = listOf(PartPayment("bad", 0, 5_000.0))
        )
        val result = useCase(inputs)
        assertTrue(result.schedule.all { it.partPayment == 0.0 })
        assertTrue(useCase.invalidPartPaymentIds(inputs).contains("bad"))
    }

    @Test
    fun amountExceedingOutstandingBalanceIsInvalid() {
        val inputs = LoanInputs(
            principal = 100_000.0,
            annualRate = 10.0,
            duration = 12,
            durationType = DurationType.MONTHS,
            partPayments = listOf(PartPayment("tooMuch", 2, 100_000.0))
        )
        assertTrue(useCase.invalidPartPaymentIds(inputs).contains("tooMuch"))
    }
}