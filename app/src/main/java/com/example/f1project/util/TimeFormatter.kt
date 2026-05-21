package com.example.f1project.util

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object TimeFormatter {

    private val PL = Locale("pl", "PL")

    private val SESSION_FORMATTER    = DateTimeFormatter.ofPattern("EEE, d MMM, HH:mm", PL)
    private val DATE_LONG_FORMATTER  = DateTimeFormatter.ofPattern("d MMMM yyyy", PL)
    private val DATE_SHORT_FORMATTER = DateTimeFormatter.ofPattern("d MMM", PL)
    private val TIME_FORMATTER       = DateTimeFormatter.ofPattern("HH:mm", PL)

    fun formatSessionDateTime(date: String?, time: String?): String {
        if (date == null || time == null) return "Brak danych"
        return try {
            ZonedDateTime.parse("${date}T${time}")
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(SESSION_FORMATTER)
        } catch (_: DateTimeParseException) { "$date $time (UTC)" }
        catch (_: Exception)             { "$date $time (UTC)" }
    }

    fun formatDateLong(dt: ZonedDateTime?): String {
        if (dt == null) return "—"
        return dt.withZoneSameInstant(ZoneId.systemDefault())
            .format(DATE_LONG_FORMATTER)
    }

    fun formatDateShort(dt: ZonedDateTime?): String {
        if (dt == null) return "—"
        return dt.withZoneSameInstant(ZoneId.systemDefault())
            .format(DATE_SHORT_FORMATTER)
    }

    fun formatTime(dt: ZonedDateTime?): String {
        if (dt == null) return "—"
        return dt.withZoneSameInstant(ZoneId.systemDefault())
            .format(TIME_FORMATTER)
    }

    fun formatCountdown(duration: Duration): String {
        if (duration.isNegative || duration.isZero) return "teraz"
        val days    = duration.toDays()
        val hours   = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        return when {
            days  > 0 -> "za ${days}d ${hours}h"
            hours > 0 -> "za ${hours}h ${minutes}min"
            else      -> "za ${minutes}min"
        }
    }

    fun formatCountdownDetailed(target: ZonedDateTime?): String {
        if (target == null) return "Brak danych"
        val duration = Duration.between(ZonedDateTime.now(), target)
        if (duration.isNegative) return "W trakcie..."
        val d = duration.toDays()
        val h = duration.toHours() % 24
        val m = duration.toMinutes() % 60
        val s = duration.seconds % 60
        return if (d > 0) "%02d D %02d H %02d M %02d S".format(d, h, m, s)
        else              "%02d H %02d M %02d S".format(h, m, s)
    }

    fun nextDailyTriggerMs(hour: Int, minute: Int): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var trigger = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!trigger.isAfter(now)) trigger = trigger.plusDays(1)
        return trigger.toInstant().toEpochMilli()
    }
}