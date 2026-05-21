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
    private const val PREFS_NAME = "f1_alarm_prefs"
    private const val KEY_CODES  = "alarm_request_codes"

    // Deterministyczne indeksy sesji — hashCode() nie jest stabilny między buildami
    private val SESSION_INDEX = mapOf(
        "Trening 1"         to 0,
        "Trening 2"         to 1,
        "Trening 3"         to 2,
        "Kwal. do Sprintu"  to 3,
        "Sprint"            to 4,
        "Kwalifikacje (GP)" to 5,
        "Wyścig"            to 6
    )

    fun scheduleAll(context: Context, races: List<DomainRace>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Zawsze anuluj stare alarmy przed zaplanowaniem nowych
        // (np. gdy użytkownik zmienia sezon lub odświeża harmonogram)
        cancelAll(context)

        val now = System.currentTimeMillis()
        val scheduledCodes = mutableSetOf<String>()

        races.forEach { race ->
            race.sessions.forEach { session ->
                val sessionMs = parseToEpochMs(session.date, session.time) ?: return@forEach
                val notifyAtMs = sessionMs - TimeUnit.MINUTES.toMillis(NOTIFY_MINUTES_BEFORE)

                if (notifyAtMs <= now) return@forEach // sesja już minęła

                val requestCode = generateRequestCode(race.round, session.name)
                scheduledCodes.add(requestCode.toString())

                val pendingIntent = buildPendingIntent(
                    context, requestCode, session.name, race.name
                )

                setAlarm(alarmManager, notifyAtMs, pendingIntent)
            }
        }

        // Zapisz kody requestCode żeby można było anulować alarmy bez listy wyścigów
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_CODES, scheduledCodes)
            .apply()
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val codes = prefs.getStringSet(KEY_CODES, emptySet()) ?: emptySet()

        codes.forEach { codeStr ->
            val code = codeStr.toIntOrNull() ?: return@forEach
            val intent = Intent(context, NotificationReceiver::class.java)
            // FLAG_NO_CREATE: zwróć null zamiast tworzyć nowy PendingIntent
            val pi = PendingIntent.getBroadcast(
                context, code, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }

        prefs.edit().remove(KEY_CODES).apply()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Hierarchia wyboru metody alarmowej:
    //
    // Android 12+ bez uprawnienia SCHEDULE_EXACT_ALARM
    //   → setAndAllowWhileIdle()  — inexact, może się spóźnić kilka minut,
    //     ale DZIAŁA w Doze Mode (WorkManager nie gwarantuje tego)
    //
    // Android 6-11 lub Android 12+ z uprawnieniem
    //   → setExactAndAllowWhileIdle() — exact + działa w Doze Mode
    //
    // Android < 6 (minSdk=24, więc niemożliwe — dla kompletności)
    //   → setExact()
    // ─────────────────────────────────────────────────────────────────────────
    private fun setAlarm(alarmManager: AlarmManager, triggerAtMs: Long, pi: PendingIntent) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms() -> {
                // Fallback: użytkownik nie przyznał SCHEDULE_EXACT_ALARM
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMs, pi
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Preferowane: exact + działa w Doze Mode
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMs, pi
                )
            }
            else -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
            }
        }
    }

    private fun buildPendingIntent(
        context: Context,
        requestCode: Int,
        sessionName: String,
        raceName: String
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.KEY_SESSION_NAME, sessionName)
            putExtra(NotificationReceiver.KEY_RACE_NAME, raceName)
            putExtra(NotificationReceiver.KEY_MINUTES_BEFORE, NOTIFY_MINUTES_BEFORE.toInt())
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // round (1-24) * 10 + sessionIdx (0-6) = unikalne kody 10..246
    // Deterministyczne — ten sam wynik zawsze dla tej samej sesji
    private fun generateRequestCode(round: Int, sessionName: String): Int {
        val sessionIdx = SESSION_INDEX[sessionName] ?: (sessionName.length % 10)
        return round * 10 + sessionIdx
    }

    private fun parseToEpochMs(date: String?, time: String?): Long? {
        if (date == null || time == null) return null
        return try {
            ZonedDateTime.parse("${date}T${time}").toInstant().toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
}