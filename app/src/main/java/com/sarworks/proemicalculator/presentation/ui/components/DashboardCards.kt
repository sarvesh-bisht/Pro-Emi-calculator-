package com.sarworks.proemicalculator.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.domain.model.TenureDisplayFormat
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel
import java.text.NumberFormat
import java.util.Locale

// Design Tokens from Requirements
val EmeraldGreen = Color(0xFF2E7D32)
val SoftGreen = Color(0xFFF0FFF4)

/**
 * Revamped Dashboard Cards - High-Contrast "Savings First" Visual Hierarchy
 */
@Composable
fun DashboardCards(state: EmiContract.State, viewModel: EmiViewModel) {
    val result = state.result
    val format = state.tenureDisplayFormat

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Monthly EMI Card
        Text(
            text = "MONTHLY EMI",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "${formatCurrency(result.standardEmi)} / month",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // 2. Savings & Impact Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAVINGS & IMPACT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Tenure Format Switch (o) Months ( ) Yrs+Mo
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = format == TenureDisplayFormat.MONTHS,
                    onClick = { viewModel.handleIntent(EmiContract.Intent.ToggleTenureFormat(TenureDisplayFormat.MONTHS)) },
                    modifier = Modifier.size(24.dp)
                )
                Text("Months", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                
                RadioButton(
                    selected = format == TenureDisplayFormat.YEARS_AND_MONTHS,
                    onClick = { viewModel.handleIntent(EmiContract.Intent.ToggleTenureFormat(TenureDisplayFormat.YEARS_AND_MONTHS)) },
                    modifier = Modifier.size(24.dp)
                )
                Text("Yrs+Mo", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        // Row 1: Interest Saved & Tenure Saved (High Contrast Green)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SavingsCard(
                title = "🎉 Interest Saved",
                value = formatCurrency(result.interestSaved),
                modifier = Modifier.weight(1f)
            )
            SavingsCard(
                title = "⏳ Tenure Saved",
                value = formatTenure(result.monthsSaved, format),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Total Interest & Total Payment (Standard Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Interest",
                value = formatCurrency(result.totalInterest),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Payment",
                value = formatCurrency(result.totalPayment),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SavingsCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SoftGreen),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = EmeraldGreen)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatCurrency(amount: Double): String {
    return try {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        formatter.format(amount)
    } catch (e: Exception) {
        "₹0"
    }
}

fun formatTenure(months: Int, format: TenureDisplayFormat): String {
    return when (format) {
        TenureDisplayFormat.MONTHS -> "$months Months"
        TenureDisplayFormat.YEARS_AND_MONTHS -> {
            val years = months / 12
            val remainingMonths = months % 12
            if (years > 0) "$years Yrs $remainingMonths Mo" else "$remainingMonths Mo"
        }
    }
}
