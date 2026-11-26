package com.sarworks.proemicalculator

import com.sarworks.proemicalculator.domain.model.DurationType
import com.sarworks.proemicalculator.domain.model.LoanInputs
import com.sarworks.proemicalculator.domain.model.LoanProfile
import com.sarworks.proemicalculator.domain.model.PartPayment
import org.junit.Assert.assertEquals
import org.junit.Test

class LoanProfileSerializationTest {

    @Test
    fun roundTripPreservesData() {
        val profile = LoanProfile(
            id = "test-id",
            name = "Home Loan",
            lender = "SBI",
            inputs = LoanInputs(
                principal = 5000000.0,
                annualRate = 8.5,
                duration = 20,
                durationType = DurationType.YEARS,
                extraEmi = 5000.0,
                partPayments = listOf(PartPayment("pp1", 12, 100000.0))
            )
        )
        val json = profile.toJson().toString()
        val restored = LoanProfile.fromJson(org.json.JSONObject(json))
        assertEquals(profile.id, restored.id)
        assertEquals(profile.name, restored.name)
        assertEquals(profile.lender, restored.lender)
        assertEquals(profile.inputs.principal, restored.inputs.principal, 1e-9)
        assertEquals(profile.inputs.annualRate, restored.inputs.annualRate, 1e-9)
        assertEquals(profile.inputs.duration, restored.inputs.duration)
        assertEquals(profile.inputs.durationType, restored.inputs.durationType)
        assertEquals(profile.inputs.extraEmi, restored.inputs.extraEmi, 1e-9)
        assertEquals(1, restored.inputs.partPayments.size)
        assertEquals(100000.0, restored.inputs.partPayments[0].amount, 1e-9)
    }

    @Test
    fun listRoundTrip() {
        val profiles = listOf(
            LoanProfile(id = "a", name = "Loan A", inputs = LoanInputs(principal = 100000.0)),
            LoanProfile(id = "b", name = "Loan B", inputs = LoanInputs(principal = 200000.0))
        )
        val json = LoanProfile.listToJsonArray(profiles)
        val restored = LoanProfile.listFromJsonArray(json)
        assertEquals(2, restored.size)
        assertEquals("a", restored[0].id)
        assertEquals("b", restored[1].id)
    }

    @Test
    fun emptyJsonReturnsEmptyList() {
        assertEquals(0, LoanProfile.listFromJsonArray("").size)
        assertEquals(0, LoanProfile.listFromJsonArray("not json").size)
    }
}