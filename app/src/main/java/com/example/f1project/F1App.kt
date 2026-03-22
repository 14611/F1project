package com.example.f1project

import android.app.Application
import com.example.f1project.data.F1Repository
import com.example.f1project.data.OpenF1Repository
import com.example.f1project.data.local.CacheManager
import com.example.f1project.notifications.NotificationHelper
import com.example.f1project.notifications.NotificationSettingsStore
import com.example.f1project.widget.F1WidgetUpdateWorker

// Klasa Application — żyje przez cały cykl życia apki
// Przechowuje singleton Repository dostępny dla ViewModelów
class F1App : Application() {

    // Lazy — tworzone przy pierwszym użyciu
    val cacheManager by lazy { CacheManager(this) }
    val repository by lazy { F1Repository(cacheManager) }

    val openF1Repository by lazy { OpenF1Repository() }

    val notificationSettingsStore by lazy { NotificationSettingsStore(this) }

    override fun onCreate() {
        super.onCreate()
        // Tworzenie kanału powiadomień przy starcie aplikacji
        NotificationHelper.createNotificationChannel(this)
        // Widget
        F1WidgetUpdateWorker.schedule(this)
        val immediateUpdate = androidx.work.OneTimeWorkRequestBuilder<F1WidgetUpdateWorker>()
            .build()
        androidx.work.WorkManager.getInstance(this).enqueue(immediateUpdate)
    }
}
