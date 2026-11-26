package com.sarworks.proemicalculator.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.ui.ads.AdMobBanner
import com.sarworks.proemicalculator.presentation.ui.components.*
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel

/**
 * Revamped EmiScreen - Integrates all modern UI components
 * High-Contrast Visual Hierarchy with Collapsible Sections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiScreen(viewModel: EmiViewModel, onNavigateToTheme: () -> Unit, onNavigateToPortfolio: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pro EMI Calculator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = com.sarworks.proemicalculator.R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                    )
                },

                actions = {
                   /* TextButton(
                        onClick = { viewModel.handleIntent(EmiContract.Intent.Reset) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontWeight = FontWeight.Medium)
                    }*/
                    IconButton(onClick = onNavigateToPortfolio) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Portfolio")
                    }
                    IconButton(onClick = onNavigateToTheme) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Select Theme")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AdMobBanner()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Editing mode banner + save/cancel
            if (state.isLoanEditingMode) {
                var loanName by remember { mutableStateOf("") }
                var lenderName by remember { mutableStateOf("") }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Editing Loan Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        OutlinedTextField(
                            value = loanName,
                            onValueChange = { loanName = it },
                            label = { Text("Loan Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lenderName,
                            onValueChange = { lenderName = it },
                            label = { Text("Lender (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.handleIntent(EmiContract.Intent.SaveLoanProfile(loanName, lenderName)); onNavigateToPortfolio() }) {
                                Text("Save Loan")
                            }
                            OutlinedButton(onClick = { viewModel.handleIntent(EmiContract.Intent.CancelEditingLoan); onNavigateToPortfolio() }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            // 1. Input Form (with Chips & Prepayments Accordion)
            InputForm(state, viewModel)

            // 2. Monthly EMI Hero & Savings
            DashboardCards(state, viewModel)

            // 3. Amortization Schedule (Yearly Accordions)
            ScheduleTable(state.result)

            // Bottom Spacing
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
