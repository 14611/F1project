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
import com.example.f1project.util.TimeFormatter
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class F1WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME                   = "f1_widget_update"
        private const val WORK_INTERVAL_MINUTES       = 30L
        private const val NETWORK_REFRESH_INTERVAL_MS = 60 * 60 * 1000L
        private const val PREFS_NAME                  = "f1_widget_prefs"
        private const val KEY_LAST_NETWORK_MS         = "last_network_fetch_ms"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<F1WidgetUpdateWorker>(
                WORK_INTERVAL_MINUTES, TimeUnit.MINUTES
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
            val repository    = (context.applicationContext as F1App).repository
            val prefs         = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentSeason = LocalDate.now().year.toString()

            // Krok 1: Spróbuj z dyskowego cache OkHttp — zero sieci
            var scheduleResult = repository.getRaceSchedule(
                season    = currentSeason,
                cacheOnly = true
            )

            // Krok 2: Cache pusty → sprawdź throttle 1h
            if (scheduleResult is RepositoryResult.Error) {
                val lastNetworkMs   = prefs.getLong(KEY_LAST_NETWORK_MS, 0L)
                val msSinceLastCall = System.currentTimeMillis() - lastNetworkMs
                val networkAllowed  = msSinceLastCall > NETWORK_REFRESH_INTERVAL_MS

                if (networkAllowed) {
                    scheduleResult = repository.getRaceSchedule(
                        season       = currentSeason,
                        forceRefresh = false
                    )
                    if (scheduleResult !is RepositoryResult.Error) {
                        prefs.edit()
                            .putLong(KEY_LAST_NETWORK_MS, System.currentTimeMillis())
                            .apply()
                    }
                } else {
                    return Result.retry()
                }
            }

            val races = when (scheduleResult) {
                is RepositoryResult.Fresh  -> scheduleResult.data.mrData.raceTable.races
                is RepositoryResult.Cached -> scheduleResult.data.mrData.raceTable.races
                is RepositoryResult.Error  -> return Result.retry()
            }

            // Krok 3: Wyznacz najbliższe sesje
            val now = ZonedDateTime.now()

            val allFutureSessions = races.flatMap { race ->
                buildList {
                    race.firstPractice?.let  { add(Triple("Trening 1",    race.raceName, it)) }
                    race.secondPractice?.let { add(Triple("Trening 2",    race.raceName, it)) }
                    race.thirdPractice?.let  { add(Triple("Trening 3",    race.raceName, it)) }
                    race.sprintQualifying?.let { add(Triple("Kwal. Sprint", race.raceName, it)) }
                    race.sprint?.let         { add(Triple("Sprint",       race.raceName, it)) }
                    race.qualifying?.let     { add(Triple("Kwalifikacje", race.raceName, it)) }
                    if (race.date != null && race.time != null) {
                        add(Triple("Wyścig", race.raceName, Session(race.date, race.time)))
                    }
                }.mapNotNull { (name, raceName, session) ->
                    if (session.date == null || session.time == null) return@mapNotNull null
                    try {
                        val dt = ZonedDateTime.parse("${session.date}T${session.time}")
                        if (dt.isAfter(now)) Triple(name, raceName, dt) else null
                    } catch (_: Exception) { null }
                }
            }.sortedBy { it.third }

            val nextSession = allFutureSessions.firstOrNull()
            val nextRace    = allFutureSessions.firstOrNull { it.first == "Wyścig" }

            // Krok 4: Zapisz do stanu widgetu
            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(F1Widget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context    = context,
                    definition = PreferencesGlanceStateDefinition,
                    glanceId   = glanceId
                ) { widgetPrefs ->
                    widgetPrefs.toMutablePreferences().apply {
                        this[F1WidgetKeys.KEY_IS_LOADING]        = "false"
                        this[F1WidgetKeys.KEY_SESSION_NAME]      = nextSession?.first ?: "Brak sesji"
                        this[F1WidgetKeys.KEY_SESSION_RACE_NAME] = nextSession?.second ?: ""
                        // Deleguje do TimeFormatter — ZoneId.systemDefault() wewnątrz
                        this[F1WidgetKeys.KEY_SESSION_DATE]      = TimeFormatter.formatDateLong(nextSession?.third)
                        this[F1WidgetKeys.KEY_SESSION_TIME]      = TimeFormatter.formatTime(nextSession?.third)
                        this[F1WidgetKeys.KEY_RACE_NAME]         = nextRace?.second ?: "Brak wyścigów"
                        this[F1WidgetKeys.KEY_RACE_DATE]         = TimeFormatter.formatDateLong(nextRace?.third)
                        this[F1WidgetKeys.KEY_RACE_TIME]         = TimeFormatter.formatTime(nextRace?.third)
                    }
                }
                F1Widget().update(context, glanceId)
            }

            Result.success()

        } catch (_: Exception) {
            Result.retry()
        }
    }
}