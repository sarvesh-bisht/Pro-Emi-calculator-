package com.sarworks.proemicalculator

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmiScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearPrefs() {
        val ctx = composeRule.activity.applicationContext
        ctx.getSharedPreferences("pro_status", Context.MODE_PRIVATE).edit().clear().apply()
        ctx.getSharedPreferences("loan_portfolio", Context.MODE_PRIVATE).edit().clear().apply()
        ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun waitForApp() {
        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodesWithText("Pro EMI Calculator", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun appLaunchesAndShowsTitle() {
        waitForApp()
        composeRule.onNodeWithText("Pro EMI Calculator").assertIsDisplayed()
    }

    @Test
    fun monthlyEmiCardIsDisplayed() {
        waitForApp()
        composeRule.onNodeWithText("MONTHLY EMI").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loanAmountFieldAcceptsInput() {
        waitForApp()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextReplacement("500000")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("500000").assertIsDisplayed()
    }

    @Test
    fun settingsIconNavigatesToThemeScreen() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Select Theme").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Select Theme").assertIsDisplayed()
    }

    @Test
    fun themeScreenShowsAllThemes() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Select Theme").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Deep Blue").assertIsDisplayed()
        composeRule.onNodeWithText("Emerald").assertIsDisplayed()
        composeRule.onNodeWithText("Crimson").assertIsDisplayed()
        composeRule.onNodeWithText("Slate").assertIsDisplayed()
        composeRule.onNodeWithText("Purple").assertIsDisplayed()
    }

    @Test
    fun themeScreenHasAppearanceSelector() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Select Theme").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun portfolioIconNavigatesToPortfolio() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Portfolio").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Loan Portfolio").assertIsDisplayed()
    }

    @Test
    fun portfolioShowsProGateForFreeUser() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Portfolio").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Pro Feature").assertIsDisplayed()
        composeRule.onNodeWithText("Unlock Pro (placeholder)").assertIsDisplayed()
    }

    @Test
    fun unlockProShowsPortfolio() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Portfolio").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Unlock Pro (placeholder)").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Portfolio Summary").assertIsDisplayed()
    }

    @Test
    fun addLoanFabVisibleAfterUnlock() {
        waitForApp()
        composeRule.onNodeWithContentDescription("Portfolio").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Unlock Pro (placeholder)").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Add Loan").assertIsDisplayed()
    }

    @Test
    fun savingsCardsDisplayed() {
        waitForApp()
        composeRule.onNodeWithText("Interest Saved", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Tenure Saved", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun amortizationScheduleDisplayed() {
        waitForApp()
        composeRule.onNodeWithText("AMORTIZATION SCHEDULE").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun chipsAreDisplayed() {
        waitForApp()
        composeRule.onNodeWithText("₹10L").assertIsDisplayed()
        composeRule.onNodeWithText("₹1Cr").assertIsDisplayed()
    }

    @Test
    fun chipAddsAmount() {
        waitForApp()
        composeRule.onNodeWithText("₹10L").performClick()
        composeRule.waitForIdle()
        // Default principal 1000000 + 1000000 = 2000000
        composeRule.onNodeWithText("2000000").assertIsDisplayed()
    }
}