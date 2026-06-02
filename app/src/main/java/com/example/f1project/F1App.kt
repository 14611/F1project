package com.example.f1project

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.f1project.data.F1Repository
import com.example.f1project.data.OpenF1Repository
import com.example.f1project.data.remote.RetrofitInstance
import com.example.f1project.notifications.NotificationHelper
import com.example.f1project.notifications.NotificationSettingsStore
import com.example.f1project.widget.F1WidgetUpdateWorker

class F1App : Application() {

    val repository by lazy {
        F1Repository(
            context      = this,
            normalApi    = RetrofitInstance.api,
            refreshApi   = RetrofitInstance.apiRefresh,
            cacheOnlyApi = RetrofitInstance.apiCacheOnly
        )
    }

    val openF1Repository by lazy { OpenF1Repository() }
    val notificationSettingsStore by lazy { NotificationSettingsStore(this) }

    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.initialize(this)
        NotificationHelper.createNotificationChannel(this)
        F1WidgetUpdateWorker.schedule(this)
        val immediateUpdate = OneTimeWorkRequestBuilder<F1WidgetUpdateWorker>().build()
        WorkManager.getInstance(this).enqueue(immediateUpdate)
    }
}