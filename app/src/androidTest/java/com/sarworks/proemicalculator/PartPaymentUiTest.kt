package com.sarworks.proemicalculator

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PartPaymentUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearPrefs() {
        val ctx = composeRule.activity.applicationContext
        ctx.getSharedPreferences("pro_status", Context.MODE_PRIVATE).edit().clear().apply()
        ctx.getSharedPreferences("loan_portfolio", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun waitForApp() {
        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodesWithText("Pro EMI Calculator", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun expandPrepayments() {
        composeRule.onNodeWithText("PREPAYMENTS", substring = true).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun prepaymentsSectionHeaderDisplayed() {
        waitForApp()
        composeRule.onNodeWithText("PREPAYMENTS", substring = true).assertIsDisplayed()
    }

    @Test
    fun addPartPaymentButtonCreatesRow() {
        waitForApp()
        expandPrepayments()
        composeRule.onNodeWithText("Add").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Remove").assertIsDisplayed()
    }

    @Test
    fun partPaymentRowHasDeleteButton() {
        waitForApp()
        expandPrepayments()
        composeRule.onNodeWithText("Add").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Remove").assertIsDisplayed()
    }

    @Test
    fun partPaymentNoPaymentsMessage() {
        waitForApp()
        expandPrepayments()
        composeRule.onNodeWithText("No part payments added yet.").assertIsDisplayed()
    }

    @Test
    fun addMultiplePartPaymentRows() {
        waitForApp()
        expandPrepayments()
        composeRule.onNodeWithText("Add").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithContentDescription("Remove").assertCountEquals(2)
    }
}