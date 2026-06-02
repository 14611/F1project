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
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionName   = intent.getStringExtra(NotificationScheduler.EXTRA_SESSION_NAME)  ?: return
        val raceName      = intent.getStringExtra(NotificationScheduler.EXTRA_RACE_NAME)     ?: return
        val minutesBefore = intent.getIntExtra(NotificationScheduler.EXTRA_MINUTES_BEFORE, 30)

        // Pokaż powiadomienie natychmiast — synchronicznie, BroadcastReceiver ma tylko 10s
        NotificationHelper.showSessionNotification(
            context       = context,
            sessionName   = sessionName,
            raceName      = raceName,
            minutesBefore = minutesBefore
        )

        // Zaplanuj kolejny alarm asynchronicznie
        // goAsync() przedłuża czas działania receivera ponad 10s
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val app    = context.applicationContext as F1App
                val season = LocalDate.now().year.toString()

                // Czytamy z OkHttp cache — zero sieci, szybkie
                val result = app.repository.getRaceSchedule(
                    season    = season,
                    cacheOnly = true
                )
                val races = when (result) {
                    is RepositoryResult.Fresh  -> result.data.mrData.raceTable.races
                    is RepositoryResult.Cached -> result.data.mrData.raceTable.races
                    is RepositoryResult.Error  -> return@launch
                }

                // ZMIANA: scheduleNext() planuje jeden alarm dla kolejnej sesji w łańcuchu
                NotificationScheduler.scheduleNext(context, RaceMapper.mapList(races))
            } finally {
                pendingResult.finish()
            }
        }
    }
}