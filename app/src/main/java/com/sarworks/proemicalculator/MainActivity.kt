package com.sarworks.proemicalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarworks.proemicalculator.presentation.ui.AnimatedSplashScreen
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.ui.EmiScreen
import com.sarworks.proemicalculator.presentation.ui.PortfolioScreen
import com.sarworks.proemicalculator.presentation.ui.ThemeScreen
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel
import com.sarworks.proemicalculator.presentation.ui.theme.ProEmiCalculatorTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            val viewModel: EmiViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            
            var showSplashScreen by remember { mutableStateOf(true) }
            var currentScreen by remember { mutableStateOf("main") }

            if (showSplashScreen) {
                AnimatedSplashScreen(onAnimationFinished = {
                    showSplashScreen = false
                })
            } else {
                val darkTheme = when (state.themeMode) {
                    EmiContract.ThemeMode.LIGHT -> false
                    EmiContract.ThemeMode.DARK -> true
                    EmiContract.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
                ProEmiCalculatorTheme(appTheme = state.selectedTheme, darkTheme = darkTheme) {
                    when (currentScreen) {
                        "main" -> EmiScreen(
                            viewModel = viewModel,
                            onNavigateToTheme = { currentScreen = "themes" },
                            onNavigateToPortfolio = { currentScreen = "portfolio" }
                        )
                        "themes" -> ThemeScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "main" }
                        )
                        "portfolio" -> PortfolioScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "main" },
                            onAddLoan = {
                                viewModel.handleIntent(EmiContract.Intent.EnterLoanEditingMode)
                                currentScreen = "main"
                            },
                            onEditLoan = { loanId ->
                                viewModel.handleIntent(EmiContract.Intent.StartEditingLoan(loanId))
                                currentScreen = "main"
                            }
                        )
                    }
                }
            }
        }
    }
}
