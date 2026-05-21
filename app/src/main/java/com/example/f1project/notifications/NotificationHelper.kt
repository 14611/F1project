package com.example.f1project.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.f1project.util.TimeFormatter
import java.time.Duration
import java.time.ZonedDateTime

data class SessionEvent(
    val raceName: String,
    val sessionName: String,
    val dateTime: ZonedDateTime
)

object NotificationHelper {

    const val CHANNEL_ID_SESSION = "f1_sessions"
    const val CHANNEL_ID_DAILY   = "f1_daily"

    private const val CHANNEL_NAME_SESSION = "Sesje F1"
    private const val CHANNEL_DESC_SESSION = "Powiadomienia przed sesjami Formuły 1"
    private const val CHANNEL_NAME_DAILY   = "Dzienny plan F1"
    private const val CHANNEL_DESC_DAILY   = "Poranne przypomnienie o nadchodzących sesjach i wyścigu"

    private const val DAILY_NOTIFICATION_ID = 2000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_SESSION,
                    CHANNEL_NAME_SESSION,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC_SESSION
                    enableVibration(true)
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_DAILY,
                    CHANNEL_NAME_DAILY,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESC_DAILY
                }
            )
        }
    }

    fun showSessionNotification(
        context: Context,
        sessionName: String,
        raceName: String,
        minutesBefore: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SESSION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Za $minutesBefore minut: $sessionName")
            .setContentText(raceName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showDailyNotification(
        context: Context,
        nextSession: SessionEvent?,
        nextRace: SessionEvent?,
        testMessage: String? = null
    ) {
        if (!hasNotificationPermission(context)) return
        if (testMessage == null && nextSession == null && nextRace == null) return

        val now   = ZonedDateTime.now()
        val style = NotificationCompat.InboxStyle().setBigContentTitle("F1 — Plan dnia")

        val summaryText: String

        if (testMessage != null) {
            style.addLine(testMessage)
            summaryText = testMessage
        } else {
            nextSession?.let {
                // Deleguje do TimeFormatter — ZoneId.systemDefault() wewnątrz
                val countdown = TimeFormatter.formatCountdown(Duration.between(now, it.dateTime))
                style.addLine("${it.sessionName}: ${it.raceName} — $countdown")
            }
            nextRace?.takeIf { it != nextSession }?.let {
                val countdown = TimeFormatter.formatCountdown(Duration.between(now, it.dateTime))
                style.addLine("Wyścig: ${it.raceName} — $countdown")
            }
            summaryText = nextSession?.let {
                val countdown = TimeFormatter.formatCountdown(Duration.between(now, it.dateTime))
                "${it.sessionName}: ${it.raceName} — $countdown"
            } ?: "Brak nadchodzących sesji"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("F1 — Plan dnia")
            .setContentText(summaryText)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(DAILY_NOTIFICATION_ID, notification)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}