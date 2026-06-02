package com.example.f1project.ui.settings

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.domain.mapper.RaceMapper
import com.example.f1project.notifications.DailyNotificationScheduler
import com.example.f1project.notifications.NotificationHelper
import com.example.f1project.notifications.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SettingsUiState(
    val notificationsEnabled: Boolean      = false,
    val dailyNotificationsEnabled: Boolean = false,
    val dailyNotificationHour: Int         = 8,
    val dailyNotificationMinute: Int       = 0,
    val isLoading: Boolean                 = true,
    // ── Stan uprawnień ────────────────────────────────────────────────────────
    // false na Android 12 (API 31-32) gdy użytkownik nie przyznał uprawnienia
    // Na API 33+ z USE_EXACT_ALARM zawsze true
    val canScheduleExactAlarms: Boolean    = true,
    // false na Android 13+ gdy użytkownik nie przyznał POST_NOTIFICATIONS
    val hasNotificationPermission: Boolean = true,
    // false gdy producent urządzenia (Xiaomi, Huawei, Samsung) optymalizuje baterię
    val isBatteryOptimizationIgnored: Boolean = true,
    // Komunikat po wysłaniu testowego powiadomienia
    val testNotificationSent: Boolean      = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app   = application as F1App
    private val store = app.notificationSettingsStore

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                store.notificationsEnabled,
                store.dailyNotificationsEnabled,
                store.dailyNotificationHour,
                store.dailyNotificationMinute
            ) { sessionEnabled, dailyEnabled, hour, minute ->
                SettingsUiState(
                    notificationsEnabled      = sessionEnabled,
                    dailyNotificationsEnabled = dailyEnabled,
                    dailyNotificationHour     = hour,
                    dailyNotificationMinute   = minute,
                    isLoading                 = false
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    canScheduleExactAlarms       = checkExactAlarmPermission(),
                    hasNotificationPermission    = checkNotificationPermission(),
                    isBatteryOptimizationIgnored = checkBatteryOptimization()
                )
            }
        }
    }

    // ── Odświeżenie stanu uprawnień (wywołaj z onResume) ─────────────────────
    fun refreshPermissions() {
        _uiState.value = _uiState.value.copy(
            canScheduleExactAlarms       = checkExactAlarmPermission(),
            hasNotificationPermission    = checkNotificationPermission(),
            isBatteryOptimizationIgnored = checkBatteryOptimization()
        )
    }

    // ── Powiadomienia przed sesjami ───────────────────────────────────────────
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            store.setNotificationsEnabled(enabled)
            if (enabled) {
                val season = LocalDate.now().year.toString()
                val result = app.repository.getRaceSchedule(season)
                val races  = when (result) {
                    is RepositoryResult.Fresh  -> result.data.mrData.raceTable.races
                    is RepositoryResult.Cached -> result.data.mrData.raceTable.races
                    is RepositoryResult.Error  -> emptyList()
                }
                if (races.isNotEmpty()) {
                    NotificationScheduler.scheduleAll(getApplication(), RaceMapper.mapList(races))
                }
            } else {
                NotificationScheduler.cancelCurrent(getApplication())
            }
        }
    }

    // ── Codzienne powiadomienie ───────────────────────────────────────────────
    fun toggleDailyNotifications(enabled: Boolean) {
        viewModelScope.launch {
            store.setDailyNotificationsEnabled(enabled)
            if (enabled) {
                val hour   = store.dailyNotificationHour.first()
                val minute = store.dailyNotificationMinute.first()
                DailyNotificationScheduler.schedule(getApplication(), hour, minute)
            } else {
                DailyNotificationScheduler.cancel(getApplication())
            }
        }
    }

    fun setDailyNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            store.setDailyNotificationTime(hour, minute)
            if (store.dailyNotificationsEnabled.first()) {
                DailyNotificationScheduler.schedule(getApplication(), hour, minute)
            }
        }
    }

    // ── Test powiadomienia — natychmiastowy, bez czekania na alarm ────────────
    fun sendTestNotification() {
        viewModelScope.launch {
            // Pobieramy najbliższą sesję z cache żeby treść była realna
            val season = LocalDate.now().year.toString()
            val result = app.repository.getRaceSchedule(season)
            val races  = when (result) {
                is RepositoryResult.Fresh  -> result.data.mrData.raceTable.races
                is RepositoryResult.Cached -> result.data.mrData.raceTable.races
                is RepositoryResult.Error  -> emptyList()
            }

            val sessionName: String
            val raceName: String

            if (races.isNotEmpty()) {
                val nextRace = races.firstOrNull() ?: races.last()
                raceName    = nextRace.raceName
                sessionName = "Kwalifikacje"
            } else {
                raceName    = "Testowy wyścig"
                sessionName = "Kwalifikacje"
            }

            // Powiadomienie przed sesją
            NotificationHelper.showSessionNotification(
                context       = getApplication(),
                sessionName   = sessionName,
                raceName      = raceName,
                minutesBefore = 30
            )

            // Powiadomienie dzienne
            NotificationHelper.showDailyNotification(
                context    = getApplication(),
                nextSession = null,
                nextRace    = null,
                testMessage = "To jest testowe powiadomienie dzienne"
            )

            _uiState.value = _uiState.value.copy(testNotificationSent = true)
        }
    }

    fun clearTestFlag() {
        _uiState.value = _uiState.value.copy(testNotificationSent = false)
    }

    // ── Sprawdzenie uprawnień ─────────────────────────────────────────────────

    private fun checkExactAlarmPermission(): Boolean {
        // USE_EXACT_ALARM (API 33+) nie potrzebuje zgody użytkownika — zawsze true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true
        // SCHEDULE_EXACT_ALARM (API 31-32) wymaga zgody
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getApplication<Application>()
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getApplication<Application>().checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun checkBatteryOptimization(): Boolean {
        val pm = getApplication<Application>()
            .getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(getApplication<Application>().packageName)
    }
}