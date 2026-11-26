package com.sarworks.proemicalculator.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarworks.proemicalculator.presentation.mvi.EmiContract
import com.sarworks.proemicalculator.presentation.viewmodel.EmiViewModel

/**
 * Theme Selection Screen with 5 Color Schemes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(viewModel: EmiViewModel, onBack: () -> Unit) {
    val state = viewModel.state.collectAsState().value
    val currentTheme = state.selectedTheme
    val currentThemeMode = state.themeMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Theme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Choose a color scheme that suits your preference",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Appearance mode selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            EmiContract.ThemeMode.LIGHT to "Light",
                            EmiContract.ThemeMode.DARK to "Dark",
                            EmiContract.ThemeMode.SYSTEM to "System"
                        ).forEach { (mode, label) ->
                            val selected = currentThemeMode == mode
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    viewModel.handleIntent(EmiContract.Intent.UpdateThemeMode(mode))
                                },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Theme Options
            item {
                ThemeCard(
                    name = "Deep Blue",
                    description = "Professional and calm",
                    primaryColor = Color(0xFF1A365D),
                    isSelected = currentTheme == EmiContract.AppTheme.DEEP_BLUE,
                    onSelect = {
                        viewModel.handleIntent(EmiContract.Intent.UpdateTheme(EmiContract.AppTheme.DEEP_BLUE))
                        onBack()
                    }
                )
            }

            item {
                ThemeCard(
                    name = "Emerald",
                    description = "Fresh and energetic",
                    primaryColor = Color(0xFF2E7D32),
                    isSelected = currentTheme == EmiContract.AppTheme.EMERALD,
                    onSelect = {
                        viewModel.handleIntent(EmiContract.Intent.UpdateTheme(EmiContract.AppTheme.EMERALD))
                        onBack()
                    }
                )
            }

            item {
                ThemeCard(
                    name = "Crimson",
                    description = "Bold and energetic",
                    primaryColor = Color(0xFFD32F2F),
                    isSelected = currentTheme == EmiContract.AppTheme.CRIMSON,
                    onSelect = {
                        viewModel.handleIntent(EmiContract.Intent.UpdateTheme(EmiContract.AppTheme.CRIMSON))
                        onBack()
                    }
                )
            }

            item {
                ThemeCard(
                    name = "Slate",
                    description = "Minimal and sophisticated",
                    primaryColor = Color(0xFF475569),
                    isSelected = currentTheme == EmiContract.AppTheme.SLATE,
                    onSelect = {
                        viewModel.handleIntent(EmiContract.Intent.UpdateTheme(EmiContract.AppTheme.SLATE))
                        onBack()
                    }
                )
            }

            item {
                ThemeCard(
                    name = "Purple",
                    description = "Creative and unique",
                    primaryColor = Color(0xFF6B46C1),
                    isSelected = currentTheme == EmiContract.AppTheme.PURPLE,
                    onSelect = {
                        viewModel.handleIntent(EmiContract.Intent.UpdateTheme(EmiContract.AppTheme.PURPLE))
                        onBack()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Individual Theme Card
 */
@Composable
fun ThemeCard(
    name: String,
    description: String,
    primaryColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                primaryColor.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, primaryColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Preview
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(primaryColor, shape = MaterialTheme.shapes.medium)
            )

            // Theme Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Selection Indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
