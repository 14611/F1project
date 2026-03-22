package com.example.f1project.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.notifications.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val isLoading: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as F1App
    private val settingsStore = app.notificationSettingsStore
    private val repository = app.repository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.notificationsEnabled.collect { enabled ->
                _uiState.value = SettingsUiState(
                    notificationsEnabled = enabled,
                    isLoading = false
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setNotificationsEnabled(enabled)

            if (enabled) {
                // ZMIANA: przekazujemy bieżący sezon do getRaceSchedule()
                val currentSeason = LocalDate.now().year.toString()
                val result = repository.getRaceSchedule(currentSeason)
                val races = when (result) {
                    is RepositoryResult.Fresh -> result.data.mrData.raceTable.races
                    is RepositoryResult.Cached -> result.data.mrData.raceTable.races
                    is RepositoryResult.Error -> emptyList()
                }
                if (races.isNotEmpty()) {
                    NotificationScheduler.scheduleAll(getApplication(), races)
                }
            } else {
                NotificationScheduler.cancelAll(getApplication())
            }
        }
    }
}