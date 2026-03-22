package com.example.f1project.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.Session
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class F1WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME = "f1_widget_update"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<F1WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val repository = (context.applicationContext as F1App).repository
            val currentSeason = LocalDate.now().year.toString()

            val scheduleResult = repository.getRaceSchedule(currentSeason)
            val races = when (scheduleResult) {
                is RepositoryResult.Fresh -> scheduleResult.data.mrData.raceTable.races
                is RepositoryResult.Cached -> scheduleResult.data.mrData.raceTable.races
                is RepositoryResult.Error -> return Result.retry()
            }

            val now = ZonedDateTime.now()

            // Zbieramy wszystkie przyszłe sesje (nie-wyścigowe)
            val allSessions = races.flatMap { race ->
                buildList {
                    race.firstPractice?.let { add(Triple("Trening 1", race.raceName, it)) }
                    race.secondPractice?.let { add(Triple("Trening 2", race.raceName, it)) }
                    race.thirdPractice?.let { add(Triple("Trening 3", race.raceName, it)) }
                    race.sprintQualifying?.let { add(Triple("Kwal. Sprint", race.raceName, it)) }
                    race.sprint?.let { add(Triple("Sprint", race.raceName, it)) }
                    race.qualifying?.let { add(Triple("Kwalifikacje", race.raceName, it)) }
                    if (race.date != null && race.time != null) {
                        add(Triple("Wyścig", race.raceName, Session(race.date, race.time)))
                    }
                }.mapNotNull { (name, raceName, session) ->
                    if (session.date == null || session.time == null) return@mapNotNull null
                    try {
                        val dt = ZonedDateTime.parse("${session.date}T${session.time}")
                        if (dt.isAfter(now)) Triple(name, raceName, dt) else null
                    } catch (e: Exception) { null }
                }
            }.sortedBy { it.third }

            val nextSession = allSessions.firstOrNull()
            val nextRace = allSessions.firstOrNull { it.first == "Wyścig" }

            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(F1Widget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = PreferencesGlanceStateDefinition,
                    glanceId = glanceId
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[F1WidgetKeys.KEY_IS_LOADING] = "false"
                        this[F1WidgetKeys.KEY_SESSION_NAME] = nextSession?.first ?: "Brak sesji"
                        this[F1WidgetKeys.KEY_SESSION_RACE_NAME] = nextSession?.second ?: ""
                        this[F1WidgetKeys.KEY_SESSION_DATE] =
                            formatDate(nextSession?.third)
                        this[F1WidgetKeys.KEY_SESSION_TIME] =
                            formatTime(nextSession?.third)
                        this[F1WidgetKeys.KEY_RACE_NAME] = nextRace?.second ?: "Brak wyścigów"
                        this[F1WidgetKeys.KEY_RACE_DATE] =
                            formatDate(nextRace?.third)
                        this[F1WidgetKeys.KEY_RACE_TIME] =
                            formatTime(nextRace?.third)
                    }
                }
                F1Widget().update(context, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // Formatuje datę na "20 marca 2026" w czasie lokalnym PL
    private fun formatDate(dt: ZonedDateTime?): String {
        if (dt == null) return "—"
        val local = dt.withZoneSameInstant(ZoneId.of("Europe/Warsaw"))
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl", "PL"))
        return local.format(formatter)
    }

    // Formatuje godzinę na "20:00" w czasie lokalnym PL
    private fun formatTime(dt: ZonedDateTime?): String {
        if (dt == null) return "—"
        val local = dt.withZoneSameInstant(ZoneId.of("Europe/Warsaw"))
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale("pl", "PL"))
        return local.format(formatter)
    }
}