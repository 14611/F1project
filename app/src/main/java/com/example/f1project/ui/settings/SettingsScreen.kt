package com.example.f1project.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.ui.theme.F1Dimens
import com.example.f1project.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.toggleNotifications(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = F1Dimens.listPaddingH,
                vertical = F1Dimens.listPaddingV
            ),
        verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
    ) {
        Text(
            text = "USTAWIENIA",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = F1Dimens.spacingXs)
        )

        ThemeSettingCard(
            isDarkTheme = isDarkTheme,
            onToggle = { themeViewModel.toggleTheme() }
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            NotificationSettingCard(
                enabled = uiState.notificationsEnabled,
                onToggle = { shouldEnable ->
                    if (shouldEnable) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleNotifications(true)
                        }
                    } else {
                        viewModel.toggleNotifications(false)
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeSettingCard(isDarkTheme: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = F1Dimens.cardPaddingH,
                    vertical = F1Dimens.cardPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.Brightness4
                    else Icons.Default.Brightness7,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Motyw aplikacji",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (isDarkTheme) "Ciemny" else "Jasny",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun NotificationSettingCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = F1Dimens.cardPaddingH,
                    vertical = F1Dimens.cardPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Powiadomienia przed sesjami",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Przypomnienie 30 minut przed każdą sesją",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = F1Dimens.spacingXs)
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}