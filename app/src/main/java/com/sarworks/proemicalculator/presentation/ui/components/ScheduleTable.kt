package com.sarworks.proemicalculator.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.domain.model.AmortizationResult
import com.sarworks.proemicalculator.domain.model.MonthlyPaymentRow
import java.text.NumberFormat
import java.util.Locale

/**
 * Mobile-Optimized Amortization Schedule
 * Grouped into Yearly Accordions with Icons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTable(result: AmortizationResult) {
    if (result.schedule.isEmpty()) return

    // Group schedule by years
    val yearlySchedule = result.schedule.groupBy { (it.month - 1) / 12 + 1 }
    var selectedRow by remember { mutableStateOf<MonthlyPaymentRow?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AMORTIZATION SCHEDULE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Yearly Accordions
        yearlySchedule.forEach { (year, months) ->
            YearAccordion(year, months) { row ->
                selectedRow = row
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedRow != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedRow = null },
            sheetState = sheetState
        ) {
            ScheduleRowDetail(selectedRow!!)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun YearAccordion(year: Int, months: List<MonthlyPaymentRow>, onRowClick: (MonthlyPaymentRow) -> Unit) {
    var isExpanded by remember { mutableStateOf(year == 1) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = { isExpanded = !isExpanded },
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = "Expand/Collapse",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Year $year (Months ${months.first().month} - ${months.last().month})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                months.forEach { monthRow ->
                    MobileScheduleCard(monthRow, onRowClick)
                }
            }
        }
    }
}

@Composable
fun MobileScheduleCard(row: MonthlyPaymentRow, onClick: (MonthlyPaymentRow) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(row) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Month ${row.month} • EMI: ${formatCurrencyCompact(row.emiComponent)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Bal: ${formatCurrencyCompact(row.closingBalance)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Principal: ${formatCurrencyCompact(row.principalPaid)} | Interest: ${formatCurrencyCompact(row.interestPaid)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ScheduleRowDetail(row: MonthlyPaymentRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Month ${row.month} Breakdown",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        DetailItem("Opening Balance", formatCurrencyCompact(row.openingBalance))
        DetailItem("EMI Component", formatCurrencyCompact(row.emiComponent))
        DetailItem("Interest Paid", formatCurrencyCompact(row.interestPaid))
        DetailItem("Principal Paid", formatCurrencyCompact(row.principalPaid))
        if (row.extraEmi > 0) DetailItem("Extra EMI", formatCurrencyCompact(row.extraEmi), color = Color(0xFF2E7D32))
        if (row.partPayment > 0) DetailItem("Part Payment", formatCurrencyCompact(row.partPayment), color = Color(0xFF2E7D32))
        DetailItem("Total Payment", formatCurrencyCompact(row.totalPayment), isBold = true)
        DetailItem("Closing Balance", formatCurrencyCompact(row.closingBalance), color = MaterialTheme.colorScheme.primary, isBold = true)
    }
}

@Composable
fun DetailItem(label: String, value: String, color: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

fun formatCurrencyCompact(amount: Double): String {
    return when {
        amount < 0.01 -> "₹0"
        amount >= 10000000 -> {
            val crores = amount / 10000000
            String.format("₹%.2f Cr", crores)
        }
        amount >= 100000 -> {
            val lakhs = amount / 100000
            String.format("₹%.2f L", lakhs)
        }
        else -> {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            formatter.format(amount)
        }
    }
}
