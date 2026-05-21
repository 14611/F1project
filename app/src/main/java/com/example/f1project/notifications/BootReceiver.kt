package com.example.f1project.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.domain.mapper.RaceMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val app   = context.applicationContext as F1App
                val store = app.notificationSettingsStore

                // ── Powiadomienia przed sesjami ───────────────────────────────
                if (store.notificationsEnabled.first()) {
                    val season = LocalDate.now().year.toString()
                    val result = app.repository.getRaceSchedule(season)
                    val races  = when (result) {
                        is RepositoryResult.Fresh  -> result.data.mrData.raceTable.races
                        is RepositoryResult.Cached -> result.data.mrData.raceTable.races
                        is RepositoryResult.Error  -> emptyList()
                    }
                    if (races.isNotEmpty()) {
                        NotificationScheduler.scheduleAll(context, RaceMapper.mapList(races))
                    }
                }

                // ── Codzienne powiadomienie ───────────────────────────────────
                if (store.dailyNotificationsEnabled.first()) {
                    val hour   = store.dailyNotificationHour.first()
                    val minute = store.dailyNotificationMinute.first()
                    DailyNotificationScheduler.schedule(context, hour, minute)
                }

            } finally {
                pendingResult.finish()
            }
        }
    }
}