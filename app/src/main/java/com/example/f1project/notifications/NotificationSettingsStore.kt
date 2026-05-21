package com.example.f1project.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "f1_settings")

class NotificationSettingsStore(private val context: Context) {

    companion object {
        val KEY_NOTIFICATIONS_ENABLED       = booleanPreferencesKey("notifications_enabled")
        val KEY_DAILY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("daily_notifications_enabled")
        val KEY_DAILY_HOUR                  = intPreferencesKey("daily_notification_hour")
        val KEY_DAILY_MINUTE                = intPreferencesKey("daily_notification_minute")
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: false }

    val dailyNotificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[KEY_DAILY_NOTIFICATIONS_ENABLED] ?: false }

    val dailyNotificationHour: Flow<Int> = context.settingsDataStore.data
        .map { it[KEY_DAILY_HOUR] ?: 8 }

    val dailyNotificationMinute: Flow<Int> = context.settingsDataStore.data
        .map { it[KEY_DAILY_MINUTE] ?: 0 }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDailyNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_DAILY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDailyNotificationTime(hour: Int, minute: Int) {
        context.settingsDataStore.edit {
            it[KEY_DAILY_HOUR]   = hour
            it[KEY_DAILY_MINUTE] = minute
        }
    }
}