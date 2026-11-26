package com.sarworks.proemicalculator.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.domain.model.LoanProfile
import com.sarworks.proemicalculator.domain.usecase.PortfolioAggregator
import com.sarworks.proemicalculator.domain.usecase.PrepaymentOptimizerUseCase
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.ui.components.formatCurrency
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: EmiViewModel,
    onBack: () -> Unit,
    onAddLoan: () -> Unit,
    onEditLoan: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isPro = state.isProUnlocked

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Portfolio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (isPro) {
                FloatingActionButton(onClick = onAddLoan) {
                    Icon(Icons.Default.Add, contentDescription = "Add Loan")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!isPro) {
            // Pro gate
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Pro Feature", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Multi-loan portfolio, prepayment optimizer, and scenario simulator are available with Pro.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { viewModel.handleIntent(EmiContract.Intent.UpdateProStatus(true)) }) {
                    Text("Unlock Pro (placeholder)")
                }
            }
        } else {
            val loans = state.loanProfiles
            val aggregator = remember { PortfolioAggregator() }
            val summary = remember(loans) { aggregator.aggregate(loans) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Aggregate summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Portfolio Summary", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Loans: ${summary.loanCount}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Total Monthly EMI: ${formatCurrency(summary.totalMonthlyEmi)}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Total Outstanding: ${formatCurrency(summary.totalOutstanding)}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Total Remaining Interest: ${formatCurrency(summary.totalRemainingInterest)}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Aggregate Payoff: ${summary.aggregatePayoffMonths} months", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // Optimizer section
                if (loans.size >= 2) {
                    item {
                        OptimizerSection(loans, viewModel)
                    }
                }

                // Loan list
                items(loans, key = { it.id }) { loan ->
                    LoanProfileCard(loan, viewModel, onEditLoan)
                }

                if (loans.isEmpty()) {
                    item {
                        Text("No loans yet. Tap + to add one.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(16.dp))
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun LoanProfileCard(loan: LoanProfile, viewModel: EmiViewModel, onEditLoan: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEditLoan(loan.id) }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(loan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (loan.lender.isNotBlank()) Text("Lender: ${loan.lender}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Text("Principal: ${formatCurrency(loan.inputs.principal)}", fontSize = 12.sp)
            Text("Rate: ${loan.inputs.annualRate}%  |  Tenure: ${loan.inputs.duration} ${if (loan.inputs.durationType.name == "YEARS") "yrs" else "mo"}", fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { viewModel.deleteLoanProfile(loan.id) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun OptimizerSection(loans: List<LoanProfile>, viewModel: EmiViewModel) {
    var surplusText by remember { mutableStateOf("") }
    var optimizerResults by remember { mutableStateOf<List<com.sarworks.proemicalculator.domain.usecase.OptimizationResult>>(emptyList()) }
    val optimizer = remember { PrepaymentOptimizerUseCase() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Prepayment Optimizer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = surplusText,
                onValueChange = { surplusText = it },
                label = { Text("Surplus (₹)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            Button(onClick = {
                val surplus = surplusText.toDoubleOrNull() ?: 0.0
                optimizerResults = optimizer.optimize(loans, surplus)
            }) {
                Text("Rank")
            }
        }
        optimizerResults.forEach { result ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(result.loanName, fontWeight = FontWeight.Bold)
                    Text("Interest saved: ${formatCurrency(result.interestSaved)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Months saved: ${result.monthsSaved}", fontSize = 12.sp)
                }
            }
        }

        // Scenario simulator
        Text("Scenario Simulator", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
        var extraText by remember { mutableStateOf("") }
        var selectedLoanId by remember { mutableStateOf(loans.firstOrNull()?.id) }
        var scenarioResult by remember { mutableStateOf<com.sarworks.proemicalculator.domain.usecase.ScenarioResult?>(null) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Loan selector (simple: first loan)
            OutlinedTextField(
                value = extraText,
                onValueChange = { extraText = it },
                label = { Text("Extra monthly EMI (₹)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            Button(onClick = {
                val extra = extraText.toDoubleOrNull() ?: 0.0
                val loan = loans.firstOrNull { it.id == selectedLoanId } ?: loans.firstOrNull()
                if (loan != null) {
                    scenarioResult = optimizer.simulateScenario(loan, extra)
                }
            }) {
                Text("Simulate")
            }
        }
        scenarioResult?.let { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Interest saved: ${formatCurrency(r.interestSaved)}", fontSize = 12.sp)
                    Text("Months saved: ${r.monthsSaved}", fontSize = 12.sp)
                    Text("New payoff: ${r.optimizedMonths} months (was ${r.baselineMonths})", fontSize = 12.sp)
                }
            }
        }
    }
}
