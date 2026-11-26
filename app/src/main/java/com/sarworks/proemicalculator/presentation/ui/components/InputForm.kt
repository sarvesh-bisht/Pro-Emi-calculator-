package com.sarworks.proemicalculator.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.domain.model.DurationType
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel

/**
 * Revamped Input Form Component
 * Allows manual decimals and uses icons for collapsible sections
 */
@Composable
fun InputForm(state: EmiContract.State, viewModel: EmiViewModel) {
    val inputs = state.inputs
    val MAX_LOAN_AMOUNT = 50000000.0  // ₹5 Crore

    var isPrepaymentsExpanded by remember { mutableStateOf(false) }

    // Use local string states to allow manual decimals while typing
    var principalText by remember(inputs.principal) { 
        mutableStateOf(if (inputs.principal == 0.0) "" else inputs.principal.toLong().toString()) 
    }
    var rateText by remember(inputs.annualRate) { 
        mutableStateOf(if (inputs.annualRate == 0.0) "" else inputs.annualRate.toString()) 
    }
    var durationText by remember(inputs.duration) { 
        mutableStateOf(if (inputs.duration == 0) "" else inputs.duration.toString()) 
    }
    var extraEmiText by remember(inputs.extraEmi) { 
        mutableStateOf(if (inputs.extraEmi == 0.0) "" else inputs.extraEmi.toString()) 
    }

    val isAmountExceeded = inputs.principal > MAX_LOAN_AMOUNT
    val isRateInvalid = inputs.annualRate < 0 || inputs.annualRate > 50
    val isDurationInvalid = inputs.duration <= 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Loan Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "LOAN DETAILS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Loan Amount Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Loan Amount (₹)", style = MaterialTheme.typography.bodySmall)
                        Text("Max ₹5 Cr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { value ->
                            principalText = value
                            val amount = value.toDoubleOrNull() ?: 0.0
                            viewModel.handleIntent(EmiContract.Intent.UpdateAmount(amount))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isAmountExceeded,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {
                            if (principalText.isNotEmpty()) {
                                IconButton(onClick = { 
                                    principalText = ""
                                    viewModel.handleIntent(EmiContract.Intent.UpdateAmount(0.0))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Amount"
                                    )
                                }
                            }
                        }
                    )
                }

                // Quick Select Chips
                val chips = listOf(
                    "₹10L" to 1000000.0,
                    "₹25L" to 2500000.0,
                    "₹50L" to 5000000.0,
                    "₹1Cr" to 10000000.0,
                    "₹5Cr" to 50000000.0
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(chips) { (label, value) ->
                        SuggestionChip(
                            onClick = { 
                                val newAmount = inputs.principal + value
                                viewModel.handleIntent(EmiContract.Intent.UpdateAmount(newAmount)) 
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                if (isAmountExceeded) {
                    Text(
                        text = "Loan amount cannot exceed ₹5 Crore",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Rate and Duration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interest Rate
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Interest Rate (%)", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { value ->
                                rateText = value
                                val rate = value.toDoubleOrNull() ?: 0.0
                                viewModel.handleIntent(EmiContract.Intent.UpdateRate(rate))
                            },
                            isError = isRateInvalid && inputs.annualRate != 0.0,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    // Duration
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Duration", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = durationText,
                                onValueChange = { value ->
                                    durationText = value
                                    val duration = value.toIntOrNull() ?: 0
                                    viewModel.handleIntent(EmiContract.Intent.UpdateDuration(duration))
                                },
                                modifier = Modifier.weight(1f),
                                isError = isDurationInvalid && inputs.duration != 0,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            // Segmented Toggle for Yrs | Mo
                            Surface(
                                modifier = Modifier.padding(start = 4.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row {
                                    val isYear = inputs.durationType == DurationType.YEARS
                                    TextButton(
                                        onClick = { viewModel.handleIntent(EmiContract.Intent.UpdateDurationType(DurationType.YEARS)) },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.width(40.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (isYear) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (isYear) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text("Yrs", fontSize = 10.sp)
                                    }
                                    TextButton(
                                        onClick = { viewModel.handleIntent(EmiContract.Intent.UpdateDurationType(DurationType.MONTHS)) },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.width(40.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (!isYear) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (!isYear) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text("Mo", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Prepayments Accordion
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { isPrepaymentsExpanded = !isPrepaymentsExpanded },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚡ PREPAYMENTS & SAVINGS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " (OPTIONAL)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Icon(
                            imageVector = if (isPrepaymentsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand/Collapse",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(visible = isPrepaymentsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Extra EMI Field
                        OutlinedTextField(
                            value = extraEmiText,
                            onValueChange = { value ->
                                extraEmiText = value
                                val extra = value.toDoubleOrNull() ?: 0.0
                                viewModel.handleIntent(EmiContract.Intent.UpdateExtraEmi(extra))
                            },
                            label = { Text("Extra Monthly EMI (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        HorizontalDivider()

                        // Part Payments Section
                        val totalMonths = if (inputs.durationType == DurationType.YEARS) inputs.duration * 12 else inputs.duration
                        PartPaymentsSection(state.inputs.partPayments, viewModel, totalMonths)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
