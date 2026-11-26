package com.sarworks.proemicalculator.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class LoanProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lender: String = "",
    val inputs: LoanInputs,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("lender", lender)
        put("createdAt", createdAt)
        put("inputs", inputsToJson(inputs))
    }

    private fun inputsToJson(inputs: LoanInputs): JSONObject = JSONObject().apply {
        put("principal", inputs.principal)
        put("annualRate", inputs.annualRate)
        put("duration", inputs.duration)
        put("durationType", inputs.durationType.name)
        put("extraEmi", inputs.extraEmi)
        val arr = JSONArray()
        inputs.partPayments.forEach { pp ->
            arr.put(JSONObject().apply {
                put("id", pp.id)
                put("month", pp.month)
                put("amount", pp.amount)
            })
        }
        put("partPayments", arr)
    }

    companion object {
        fun fromJson(json: JSONObject): LoanProfile = LoanProfile(
            id = json.optString("id", UUID.randomUUID().toString()),
            name = json.optString("name", "Untitled Loan"),
            lender = json.optString("lender", ""),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            inputs = jsonToInputs(json.optJSONObject("inputs"))
        )

        private fun jsonToInputs(json: JSONObject?): LoanInputs {
            if (json == null) return LoanInputs()
            val ppArr = json.optJSONArray("partPayments")
            val partPayments = mutableListOf<PartPayment>()
            if (ppArr != null) {
                for (i in 0 until ppArr.length()) {
                    val pp = ppArr.optJSONObject(i) ?: continue
                    partPayments.add(
                        PartPayment(
                            id = pp.optString("id", UUID.randomUUID().toString()),
                            month = pp.optInt("month", 0),
                            amount = pp.optDouble("amount", 0.0)
                        )
                    )
                }
            }
            val durationType = try {
                DurationType.valueOf(json.optString("durationType", "YEARS"))
            } catch (e: Exception) {
                DurationType.YEARS
            }
            return LoanInputs(
                principal = json.optDouble("principal", 0.0),
                annualRate = json.optDouble("annualRate", 0.0),
                duration = json.optInt("duration", 0),
                durationType = durationType,
                extraEmi = json.optDouble("extraEmi", 0.0),
                partPayments = partPayments
            )
        }

        fun listFromJsonArray(json: String): List<LoanProfile> {
            val result = mutableListOf<LoanProfile>()
            if (json.isBlank()) return result
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    result.add(fromJson(obj))
                }
            } catch (e: Exception) {
                // Return empty on parse failure
            }
            return result
        }

        fun listToJsonArray(profiles: List<LoanProfile>): String {
            val arr = JSONArray()
            profiles.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }
    }
}