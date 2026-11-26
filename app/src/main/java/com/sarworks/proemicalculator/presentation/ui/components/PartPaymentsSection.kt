package com.sarworks.proemicalculator.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.domain.model.PartPayment
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel

/**
 * Part Payments UI Component
 * Fixed decimal handling and manual input
 */
@Composable
fun PartPaymentsSection(
    partPayments: List<PartPayment>,
    viewModel: EmiViewModel,
    totalMonths: Int
) {
    // Keep a live reference to inputs for validation context
    val currentInputs = viewModel.state.collectAsState().value.inputs
    val invalidIds = viewModel.invalidPartPaymentIds()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Title and Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Part Payments",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { viewModel.handleIntent(EmiContract.Intent.AddPartPaymentRow) },
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        // Part Payments List
        if (partPayments.isEmpty()) {
            Text(
                text = "No part payments added yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                partPayments.forEach { partPayment ->
                    PartPaymentRow(
                        partPayment = partPayment,
                        viewModel = viewModel,
                        totalMonths = totalMonths,
                        isInvalid = partPayment.id in invalidIds,
                        loanPrincipal = currentInputs.principal
                    )
                }
            }
        }
    }
}

/**
 * Individual Part Payment Row with stable local state for manual input
 */
@Composable
fun PartPaymentRow(
    partPayment: PartPayment,
    viewModel: EmiViewModel,
    totalMonths: Int,
    isInvalid: Boolean,
    loanPrincipal: Double
) {
    var monthText by remember(partPayment.id) {
        mutableStateOf(if (partPayment.month == 0) "" else partPayment.month.toString())
    }
    var amountText by remember(partPayment.id) {
        mutableStateOf(formatPartAmount(partPayment.amount))
    }

    // Sync local text with model changes (e.g. Reset) without clobbering in-progress input.
    LaunchedEffect(partPayment.month) {
        val desired = if (partPayment.month == 0) "" else partPayment.month.toString()
        if (monthText.toIntOrNull() != partPayment.month) {
            monthText = desired
        }
    }
    LaunchedEffect(partPayment.amount) {
        val desired = formatPartAmount(partPayment.amount)
        if (amountText.toDoubleOrNull() != partPayment.amount) {
            amountText = desired
        }
    }

    val monthError = monthText.isNotEmpty() && (monthText.toIntOrNull() == null ||
        (monthText.toIntOrNull() ?: 0) < 1 || (monthText.toIntOrNull() ?: 0) > totalMonths)
    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val amountError = amountText.isNotEmpty() && (amountValue <= 0 || isInvalid)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Input
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { value ->
                        monthText = value
                        val month = value.toIntOrNull() ?: 0
                        viewModel.handleIntent(
                            EmiContract.Intent.UpdatePartPayment(
                                id = partPayment.id,
                                month = month,
                                amount = partPayment.amount
                            )
                        )
                    },
                    label = { Text("Month", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    isError = monthError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        amountText = value
                        val amount = value.toDoubleOrNull() ?: 0.0
                        viewModel.handleIntent(
                            EmiContract.Intent.UpdatePartPayment(
                                id = partPayment.id,
                                month = partPayment.month,
                                amount = amount
                            )
                        )
                    },
                    label = { Text("Amount (₹)", fontSize = 10.sp) },
                    modifier = Modifier.weight(2f),
                    isError = amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                // Delete Button
                IconButton(
                    onClick = {
                        viewModel.handleIntent(
                            EmiContract.Intent.RemovePartPayment(partPayment.id)
                        )
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (monthError) {
                Text(
                    text = "Month must be between 1 and $totalMonths",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            } else if (amountError) {
                val msg = if (isInvalid && amountValue > 0) "Amount exceeds available balance"
                else "Amount must be greater than zero"
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun formatPartAmount(amount: Double): String {
    return if (amount == 0.0) "" else if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
}