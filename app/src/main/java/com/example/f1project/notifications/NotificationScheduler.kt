package com.example.f1project.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.f1project.data.remote.Race
import java.time.ZonedDateTime
import java.time.Duration
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    // Ile minut przed sesją wysłać powiadomienie
    private const val NOTIFY_MINUTES_BEFORE = 30L

    // Unikalny tag dla wszystkich powiadomień F1 — pozwala je wszystkie anulować
    private const val WORK_TAG = "f1_session_notification"

    // Zaplanowanie powiadomień dla wszystkich przyszłych sesji w kalendarzu
    fun scheduleAll(context: Context, races: List<Race>) {
        val workManager = WorkManager.getInstance(context)

        // Najpierw anulujemy wszystkie stare powiadomienia
        workManager.cancelAllWorkByTag(WORK_TAG)

        val now = ZonedDateTime.now()

        races.forEach { race ->
            // Zbieramy wszystkie sesje z wyścigu
            getAllSessions(race).forEach { (sessionName, sessionDateTime) ->

                // Czas do powiadomienia = czas sesji - 30 minut
                val notifyAt = sessionDateTime.minusMinutes(NOTIFY_MINUTES_BEFORE)
                val delay = Duration.between(now, notifyAt)

                // Planujemy tylko przyszłe sesje
                if (delay.isNegative) return@forEach

                val inputData = Data.Builder()
                    .putString(SessionNotificationWorker.KEY_SESSION_NAME, sessionName)
                    .putString(SessionNotificationWorker.KEY_RACE_NAME, race.raceName)
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

    // Anulowanie wszystkich zaplanowanych powiadomień
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    // Pomocnicza funkcja wyciągająca wszystkie sesje z obiektu Race
    private fun getAllSessions(race: Race): List<Pair<String, ZonedDateTime>> {
        return listOfNotNull(
            parseSession(race.firstPractice?.date, race.firstPractice?.time, "Trening 1"),
            parseSession(race.secondPractice?.date, race.secondPractice?.time, "Trening 2"),
            parseSession(race.thirdPractice?.date, race.thirdPractice?.time, "Trening 3"),
            parseSession(race.sprintQualifying?.date, race.sprintQualifying?.time, "Kwal. do Sprintu"),
            parseSession(race.sprint?.date, race.sprint?.time, "Sprint"),
            parseSession(race.qualifying?.date, race.qualifying?.time, "Kwalifikacje"),
            parseSession(race.date, race.time, "Wyścig")
        )
    }

    private fun parseSession(
        date: String?,
        time: String?,
        name: String
    ): Pair<String, ZonedDateTime>? {
        if (date == null || time == null) return null
        return try {
            val dt = ZonedDateTime.parse("${date}T${time}")
            Pair(name, dt)
        } catch (e: Exception) {
            null
        }
    }
}