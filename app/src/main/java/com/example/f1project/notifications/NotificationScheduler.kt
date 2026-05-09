package com.example.f1project.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.f1project.domain.model.DomainRace  // ZMIANA: DomainRace zamiast Race
import java.time.ZonedDateTime
import java.time.Duration
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val NOTIFY_MINUTES_BEFORE = 30L
    private const val WORK_TAG = "f1_session_notification"

    fun scheduleAll(context: Context, races: List<DomainRace>) {  // ZMIANA
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_TAG)

        val now = ZonedDateTime.now()

        races.forEach { race ->
            race.sessions.forEach { session ->  // ZMIANA: sessions już jest listą DomainSession
                val notifyAt = parseSession(session.date, session.time)
                    ?.minusMinutes(NOTIFY_MINUTES_BEFORE) ?: return@forEach

                val delay = Duration.between(now, notifyAt)
                if (delay.isNegative) return@forEach

                val inputData = Data.Builder()
                    .putString(SessionNotificationWorker.KEY_SESSION_NAME, session.name)
                    .putString(SessionNotificationWorker.KEY_RACE_NAME, race.name)
                    .putInt(
                        SessionNotificationWorker.KEY_MINUTES_BEFORE,
                        NOTIFY_MINUTES_BEFORE.toInt()
                    )
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<SessionNotificationWorker>()
                    .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(WORK_TAG)
                    .build()

                workManager.enqueue(workRequest)
            }
        }
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    private fun parseSession(date: String?, time: String?): ZonedDateTime? {
        if (date == null || time == null) return null
        return try {
            ZonedDateTime.parse("${date}T${time}")
        } catch (e: Exception) {
            null
        }
    }
}