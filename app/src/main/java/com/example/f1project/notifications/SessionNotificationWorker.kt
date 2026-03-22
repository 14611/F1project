package com.example.f1project.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// Worker wykonywany przez WorkManager w tle
// Gdy zostanie wywołany — po prostu pokazuje powiadomienie
class SessionNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        // Klucze dla danych przekazywanych do Workera
        const val KEY_SESSION_NAME = "session_name"
        const val KEY_RACE_NAME = "race_name"
        const val KEY_MINUTES_BEFORE = "minutes_before"
    }

    override suspend fun doWork(): Result {
        val sessionName = inputData.getString(KEY_SESSION_NAME) ?: return Result.failure()
        val raceName = inputData.getString(KEY_RACE_NAME) ?: return Result.failure()
        val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 30)

        NotificationHelper.showSessionNotification(
            context = context,
            sessionName = sessionName,
            raceName = raceName,
            minutesBefore = minutesBefore
        )

        return Result.success()
    }
}