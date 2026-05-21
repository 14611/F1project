package com.example.f1project.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.Race
import com.example.f1project.data.remote.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class DailyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val app = context.applicationContext as F1App

                // Nie pokazuj jeśli użytkownik wyłączył codzienne powiadomienia
                if (!app.notificationSettingsStore.dailyNotificationsEnabled.first()) return@launch

                val season = LocalDate.now().year.toString()
                // OkHttp cache na dysku — zero sieci, szybkie
                val races = when (val r = app.repository.getRaceSchedule(season)) {
                    is RepositoryResult.Fresh  -> r.data.mrData.raceTable.races
                    is RepositoryResult.Cached -> r.data.mrData.raceTable.races
                    is RepositoryResult.Error  -> emptyList()
                }

                val now = ZonedDateTime.now()
                val futureSessions = races
                    .flatMap { race -> extractSessions(race) }
                    .filter { it.dateTime.isAfter(now) }
                    .sortedBy { it.dateTime }

                val nextSession = futureSessions.firstOrNull()
                val nextRace    = futureSessions.firstOrNull { it.sessionName == "Wyścig" }

                // Pokaż powiadomienie tylko gdy jest co pokazać
                if (nextSession != null || nextRace != null) {
                    NotificationHelper.showDailyNotification(context, nextSession, nextRace)
                }

                // Zaplanuj alarm na jutro o tej samej porze
                val hour   = app.notificationSettingsStore.dailyNotificationHour.first()
                val minute = app.notificationSettingsStore.dailyNotificationMinute.first()
                DailyNotificationScheduler.schedule(context, hour, minute)

            } finally {
                pendingResult.finish()
            }
        }
    }

    // ── Pomocnicze ──────────────────────────────────────────────────────────

    private fun extractSessions(race: Race): List<SessionEvent> = listOfNotNull(
        parse(race, race.firstPractice,    "Trening 1"),
        parse(race, race.secondPractice,   "Trening 2"),
        parse(race, race.thirdPractice,    "Trening 3"),
        parse(race, race.sprintQualifying, "Kwal. Sprint"),
        parse(race, race.sprint,           "Sprint"),
        parse(race, race.qualifying,       "Kwalifikacje"),
        parseRace(race)
    )

    private fun parse(race: Race, session: Session?, name: String): SessionEvent? {
        val date = session?.date ?: return null
        val time = session.time  ?: return null
        return try {
            SessionEvent(race.raceName, name, ZonedDateTime.parse("${date}T${time}"))
        } catch (_: DateTimeParseException) { null }
    }

    private fun parseRace(race: Race): SessionEvent? {
        val date = race.date ?: return null
        val time = race.time ?: return null
        return try {
            SessionEvent(race.raceName, "Wyścig", ZonedDateTime.parse("${date}T${time}"))
        } catch (_: DateTimeParseException) { null }
    }
}