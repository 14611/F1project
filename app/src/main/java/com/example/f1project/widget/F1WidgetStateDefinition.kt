package com.example.f1project.widget

import androidx.datastore.preferences.core.stringPreferencesKey

object F1WidgetKeys {
    val KEY_IS_LOADING = stringPreferencesKey("is_loading")
    val KEY_SESSION_NAME = stringPreferencesKey("session_name")
    val KEY_SESSION_RACE_NAME = stringPreferencesKey("session_race_name")
    val KEY_SESSION_DATE = stringPreferencesKey("session_date")
    val KEY_SESSION_TIME = stringPreferencesKey("session_time")
    val KEY_RACE_NAME = stringPreferencesKey("race_name")
    val KEY_RACE_DATE = stringPreferencesKey("race_date")
    val KEY_RACE_TIME = stringPreferencesKey("race_time")
}