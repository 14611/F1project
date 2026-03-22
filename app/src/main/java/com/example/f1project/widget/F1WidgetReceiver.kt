package com.example.f1project.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class F1WidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = F1Widget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Natychmiastowe odświeżenie przy każdej aktualizacji widżetu
        triggerImmediateUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Pierwsze dodanie widżetu — uruchamiamy jednorazowy worker natychmiast
        triggerImmediateUpdate(context)
        // Planujemy też cykliczne odświeżanie
        F1WidgetUpdateWorker.schedule(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            triggerImmediateUpdate(context)
        }
    }

    private fun triggerImmediateUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<F1WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}