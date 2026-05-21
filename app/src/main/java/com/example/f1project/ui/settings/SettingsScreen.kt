package com.example.f1project.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1project.ui.theme.F1Dimens
import com.example.f1project.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val context     = LocalContext.current
    val lifecycle   = LocalLifecycleOwner.current

    var showTimePicker by remember { mutableStateOf(false) }

    // Odśwież stan uprawnień gdy wracamy z Ustawień systemowych
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshPermissions()
        }
    }

    // Schowaj "wysłano" po 3 sekundach
    LaunchedEffect(uiState.testNotificationSent) {
        if (uiState.testNotificationSent) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearTestFlag()
        }
    }

    val postNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshPermissions()
        if (granted) viewModel.toggleNotifications(true)
    }

    val dailyNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshPermissions()
        if (granted) viewModel.toggleDailyNotifications(true)
    }

    // ── Time Picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = uiState.dailyNotificationHour,
            initialMinute = uiState.dailyNotificationMinute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDailyNotificationTime(
                        timePickerState.hour, timePickerState.minute
                    )
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            },
            title = { Text("Godzina powiadomienia") },
            text  = { TimePicker(state = timePickerState) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = F1Dimens.listPaddingH, vertical = F1Dimens.listPaddingV),
        verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
    ) {
        Text(
            text     = "USTAWIENIA",
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = F1Dimens.spacingXs)
        )

        // ── Banery uprawnień ──────────────────────────────────────────────────
        if (!uiState.hasNotificationPermission) {
            PermissionBanner(
                icon    = Icons.Default.Warning,
                message = "Brak uprawnienia do powiadomień. Kliknij żeby przyznać.",
                color   = MaterialTheme.colorScheme.error,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
        }

        if (!uiState.canScheduleExactAlarms) {
            PermissionBanner(
                icon    = Icons.Default.Warning,
                message = "Brak uprawnienia do dokładnych alarmów (Android 12). Kliknij żeby otworzyć ustawienia.",
                color   = MaterialTheme.colorScheme.error,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }
            )
        }

        if (!uiState.isBatteryOptimizationIgnored) {
            PermissionBanner(
                icon    = Icons.Default.Warning,
                message = "Optymalizacja baterii może blokować powiadomienia. Kliknij żeby wyłączyć dla tej aplikacji.",
                color   = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            )
        }

        // ── Motyw ─────────────────────────────────────────────────────────────
        SettingCard(
            icon     = if (isDarkTheme) Icons.Default.Brightness4 else Icons.Default.Brightness7,
            title    = "Motyw aplikacji",
            subtitle = if (isDarkTheme) "Ciemny" else "Jasny",
            checked  = isDarkTheme,
            onToggle = { themeViewModel.toggleTheme() }
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color    = MaterialTheme.colorScheme.primary
            )
            return@Column
        }

        // ── Powiadomienia przed sesjami ───────────────────────────────────────
        SettingCard(
            icon     = Icons.Default.NotificationsActive,
            title    = "Powiadomienia przed sesjami",
            subtitle = "Przypomnienie 30 minut przed każdą sesją",
            checked  = uiState.notificationsEnabled,
            onToggle = { shouldEnable ->
                if (shouldEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.toggleNotifications(shouldEnable)
                }
            }
        )

        // ── Codzienne powiadomienie ───────────────────────────────────────────
        DailyNotificationCard(
            enabled     = uiState.dailyNotificationsEnabled,
            hour        = uiState.dailyNotificationHour,
            minute      = uiState.dailyNotificationMinute,
            onToggle    = { shouldEnable ->
                if (shouldEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    dailyNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.toggleDailyNotifications(shouldEnable)
                }
            },
            onTimeClick = { showTimePicker = true }
        )

        // ── Test powiadomień ──────────────────────────────────────────────────
        TestNotificationCard(
            sent    = uiState.testNotificationSent,
            onClick = { viewModel.sendTestNotification() }
        )
    }
}

// ── Komponenty ────────────────────────────────────────────────────────────────

@Composable
private fun PermissionBanner(
    icon: ImageVector,
    message: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape     = RoundedCornerShape(F1Dimens.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = F1Dimens.cardPaddingH,
                vertical   = F1Dimens.spacingM
            ),
            horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(
                text  = message,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
private fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = F1Dimens.cardPaddingH, vertical = F1Dimens.cardPaddingV),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM),
                modifier              = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(title,    style = MaterialTheme.typography.bodyLarge)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = F1Dimens.spacingXs))
                }
            }
            Switch(
                checked         = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun DailyNotificationCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = F1Dimens.cardPaddingH,
                vertical   = F1Dimens.cardPaddingV
            ),
            verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM),
                    modifier              = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Today, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Codzienne przypomnienie",
                            style = MaterialTheme.typography.bodyLarge)
                        Text("Plan sesji i wyścigów każdego dnia",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = F1Dimens.spacingXs))
                    }
                }
                Switch(
                    checked         = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            if (enabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTimeClick() }
                        .padding(vertical = F1Dimens.spacingXs),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Godzina powiadomienia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text  = "%02d:%02d".format(hour, minute),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TestNotificationCard(
    sent: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(F1Dimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = F1Dimens.cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = F1Dimens.cardPaddingH,
                vertical   = F1Dimens.cardPaddingV
            ),
            verticalArrangement = Arrangement.spacedBy(F1Dimens.spacingS)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(F1Dimens.spacingM)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column {
                    Text("Test powiadomień",
                        style = MaterialTheme.typography.bodyLarge)
                    Text("Wyślij testowe powiadomienia natychmiast",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = F1Dimens.spacingXs))
                }
            }

            if (sent) {
                Text(
                    text     = "✓ Powiadomienia wysłane — sprawdź szufladę powiadomień",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = F1Dimens.spacingXs)
                )
            }

            OutlinedButton(
                onClick  = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Wyślij test")
            }
        }
    }
}