package com.sarworks.proemicalculator

import com.sarworks.proemicalculator.domain.model.DurationType
import com.sarworks.proemicalculator.domain.model.LoanInputs
import com.sarworks.proemicalculator.domain.model.LoanProfile
import com.sarworks.proemicalculator.domain.usecase.PrepaymentOptimizerUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepaymentOptimizerTest {

    private val optimizer = PrepaymentOptimizerUseCase()

    private fun profile(id: String, name: String, principal: Double, rate: Double): LoanProfile =
        LoanProfile(id = id, name = name, inputs = LoanInputs(principal = principal, annualRate = rate, duration = 12, durationType = DurationType.MONTHS))

    @Test
    fun ranksHigherRateLoanFirst() {
        val loans = listOf(
            profile("a", "Low Rate", 100000.0, 8.0),
            profile("b", "High Rate", 100000.0, 12.0)
        )
        val results = optimizer.optimize(loans, 10000.0)
        assertEquals(2, results.size)
        assertEquals("b", results[0].loanId)
    }

    @Test
    fun zeroSurplusReturnsEmpty() {
        val loans = listOf(profile("a", "Loan", 100000.0, 10.0))
        assertTrue(optimizer.optimize(loans, 0.0).isEmpty())
    }

    @Test
    fun singleLoanProducesResult() {
        val loans = listOf(profile("a", "Loan", 100000.0, 10.0))
        val results = optimizer.optimize(loans, 5000.0)
        assertEquals(1, results.size)
        assertTrue(results[0].interestSaved > 0)
    }

    @Test
    fun scenarioSimulationShowsSavings() {
        val p = profile("a", "Loan", 100000.0, 10.0)
        val result = optimizer.simulateScenario(p, 1000.0)
        assertTrue(result.interestSaved > 0)
        assertTrue(result.monthsSaved >= 0)
    }
}