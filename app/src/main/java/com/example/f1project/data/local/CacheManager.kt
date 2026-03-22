package com.example.f1project.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "f1_cache")

class CacheManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        fun makeScheduleKey(season: String) =
            stringPreferencesKey("race_schedule_$season")

        fun makeDriverStandingsKey(season: String) =
            stringPreferencesKey("driver_standings_$season")

        fun makeConstructorStandingsKey(season: String) =
            stringPreferencesKey("constructor_standings_$season")

        fun makeDriverDetailKey(driverId: String) =
            stringPreferencesKey("driver_detail_$driverId")

        fun makeDriverResultsKey(season: String, driverId: String) =
            stringPreferencesKey("driver_results_${season}_$driverId")

        fun makeConstructorDetailKey(constructorId: String) =
            stringPreferencesKey("constructor_detail_$constructorId")

        fun makeConstructorResultsKey(season: String, constructorId: String) =
            stringPreferencesKey("constructor_results_${season}_$constructorId")
    }

    suspend fun <T> save(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        data: T
    ) {
        context.dataStore.edit { prefs ->
            prefs[key] = gson.toJson(data)
        }
    }

    suspend fun <T> load(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        type: java.lang.reflect.Type
    ): T? {
        return try {
            val prefs = context.dataStore.data.first()
            val json = prefs[key] ?: return null
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}