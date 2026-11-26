package com.sarworks.proemicalculator.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.domain.model.TenureDisplayFormat
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel

/**
 * Tenure Format Toggle Component
 * Allows switching between "In Months" and "Years & Months" display format
 */
@Composable
fun TenureFormatToggle(
    currentFormat: TenureDisplayFormat,
    viewModel: EmiViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Label
        Text(
            text = "Display Tenure As",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Button Row for Format Toggle
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    viewModel.handleIntent(
                        EmiContract.Intent.ToggleTenureFormat(TenureDisplayFormat.MONTHS)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                enabled = currentFormat != TenureDisplayFormat.MONTHS
            ) {
                Text("In Months", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    viewModel.handleIntent(
                        EmiContract.Intent.ToggleTenureFormat(TenureDisplayFormat.YEARS_AND_MONTHS)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                enabled = currentFormat != TenureDisplayFormat.YEARS_AND_MONTHS
            ) {
                Text("Years & Months", fontSize = 12.sp)
            }
        }
    }
}
