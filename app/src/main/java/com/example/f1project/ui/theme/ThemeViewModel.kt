package com.example.f1project.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.notifications.NotificationSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")
private val KEY_IS_DARK = booleanPreferencesKey("is_dark_theme")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _isDarkTheme = MutableStateFlow(true) // domyślnie ciemny
    val isDarkTheme = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            context.themeDataStore.data
                .map { prefs -> prefs[KEY_IS_DARK] ?: true }
                .collect { _isDarkTheme.value = it }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newValue = !_isDarkTheme.value
            context.themeDataStore.edit { prefs ->
                prefs[KEY_IS_DARK] = newValue
            }
        }
    }
}