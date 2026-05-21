package com.example.f1project.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.f1project.util.TimeFormatter

object DailyNotificationScheduler {

    private const val REQUEST_CODE = 9999

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context)

        // Anuluj poprzedni alarm przed zaplanowaniem nowego
        alarmManager.cancel(pendingIntent)

        val triggerMs = TimeFormatter.nextDailyTriggerMs(hour, minute)
        setExact(alarmManager, triggerMs, pendingIntent)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context))
    }

    private fun setExact(alarmManager: AlarmManager, triggerMs: Long, pi: PendingIntent) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms() ->
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            else ->
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        }
    }

    private fun buildPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, DailyNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}