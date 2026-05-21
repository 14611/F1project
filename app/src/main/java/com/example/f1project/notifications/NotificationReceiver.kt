package com.example.f1project.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionName  = intent.getStringExtra(KEY_SESSION_NAME)  ?: return
        val raceName     = intent.getStringExtra(KEY_RACE_NAME)     ?: return
        val minutesBefore = intent.getIntExtra(KEY_MINUTES_BEFORE, 30)

        NotificationHelper.showSessionNotification(
            context       = context,
            sessionName   = sessionName,
            raceName      = raceName,
            minutesBefore = minutesBefore
        )
    }

    companion object {
        const val KEY_SESSION_NAME   = "session_name"
        const val KEY_RACE_NAME      = "race_name"
        const val KEY_MINUTES_BEFORE = "minutes_before"
    }
}