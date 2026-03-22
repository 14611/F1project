package com.example.f1project.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "f1_settings")

class NotificationSettingsStore(private val context: Context) {

    companion object {
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    // Flow emitujący aktualną wartość ustawienia
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[KEY_NOTIFICATIONS_ENABLED] ?: false }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}