package com.sarworks.proemicalculator

import com.sarworks.proemicalculator.domain.model.DurationType
import com.sarworks.proemicalculator.domain.model.LoanInputs
import com.sarworks.proemicalculator.domain.model.LoanProfile
import com.sarworks.proemicalculator.domain.usecase.PortfolioAggregator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioAggregatorTest {

    private val aggregator = PortfolioAggregator()

    @Test
    fun aggregatesMultipleLoans() {
        val loans = listOf(
            LoanProfile(id = "a", name = "Home", inputs = LoanInputs(principal = 100000.0, annualRate = 10.0, duration = 12, durationType = DurationType.MONTHS)),
            LoanProfile(id = "b", name = "Car", inputs = LoanInputs(principal = 50000.0, annualRate = 12.0, duration = 24, durationType = DurationType.MONTHS))
        )
        val summary = aggregator.aggregate(loans)
        assertEquals(2, summary.loanCount)
        assertTrue(summary.totalMonthlyEmi > 0)
        assertTrue(summary.totalOutstanding > 0)
        assertTrue(summary.totalRemainingInterest > 0)
        assertTrue(summary.aggregatePayoffMonths > 0)
    }

    @Test
    fun emptyPortfolio() {
        val summary = aggregator.aggregate(emptyList())
        assertEquals(0, summary.loanCount)
        assertEquals(0.0, summary.totalMonthlyEmi, 1e-9)
    }
}