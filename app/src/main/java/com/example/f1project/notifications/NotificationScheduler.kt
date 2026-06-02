package com.example.f1project.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.f1project.domain.model.DomainRace
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val NOTIFY_MINUTES_BEFORE = 30L

    // Stały requestCode — jeden aktywny alarm sesyjny naraz
    // Nowy alarm nadpisuje stary przez FLAG_UPDATE_CURRENT
    private const val REQUEST_CODE = 1001

    const val EXTRA_SESSION_NAME   = "session_name"
    const val EXTRA_RACE_NAME      = "race_name"
    const val EXTRA_MINUTES_BEFORE = "minutes_before"

    // ZMIANA: zamiast planować 100+ alarmów jednocześnie (LimitExceededException na API 31+),
    // wyznaczamy JEDNĄ najbliższą przyszłą sesję i ustawiamy dla niej jeden alarm.
    // NotificationReceiver po odpaleniu wywołuje scheduleNext(), który planuje kolejny alarm.
    // Łańcuch: alarm → pokazuje powiadomienie → planuje alarm dla następnej sesji → ...
    fun scheduleAll(context: Context, races: List<DomainRace>) {
        val now = System.currentTimeMillis()

        // Zbieramy wszystkie przyszłe sesje ze wszystkich wyścigów
        val nextSession = races
            .flatMap { race ->
                race.sessions.mapNotNull { session ->
                    val sessionMs = parseToEpochMs(session.date, session.time)
                        ?: return@mapNotNull null
                    val notifyAtMs = sessionMs - TimeUnit.MINUTES.toMillis(NOTIFY_MINUTES_BEFORE)
                    if (notifyAtMs <= now) return@mapNotNull null
                    // Triple: (czas powiadomienia, nazwa sesji, nazwa wyścigu)
                    Triple(notifyAtMs, session.name, race.name)
                }
            }
            .minByOrNull { it.first } // chronologicznie najbliższa

        if (nextSession == null) {
            cancelCurrent(context)
            return
        }

        val (triggerMs, sessionName, raceName) = nextSession
        scheduleOne(context, triggerMs, sessionName, raceName)
    }

    // Wołane przez NotificationReceiver po pokazaniu powiadomienia —
    // planuje alarm dla kolejnej sesji w łańcuchu
    fun scheduleNext(context: Context, races: List<DomainRace>) {
        scheduleAll(context, races)
    }

    fun cancelCurrent(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, "", ""))
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun scheduleOne(
        context: Context,
        triggerMs: Long,
        sessionName: String,
        raceName: String
    ) {
        val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, sessionName, raceName)

        // Anuluj poprzedni alarm przed zaplanowaniem nowego
        alarmManager.cancel(pendingIntent)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms() ->
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent
                )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent
                )
            else ->
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    private fun buildPendingIntent(
        context: Context,
        sessionName: String,
        raceName: String
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_SESSION_NAME,   sessionName)
            putExtra(EXTRA_RACE_NAME,      raceName)
            putExtra(EXTRA_MINUTES_BEFORE, NOTIFY_MINUTES_BEFORE.toInt())
        }
        // FLAG_UPDATE_CURRENT — nowe dane nadpisują stary alarm o tym samym requestCode
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseToEpochMs(date: String?, time: String?): Long? {
        if (date == null || time == null) return null
        return try {
            ZonedDateTime.parse("${date}T${time}").toInstant().toEpochMilli()
        } catch (_: Exception) { null }
    }
}