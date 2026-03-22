package com.example.f1project.data

import com.example.f1project.data.local.CacheManager
import com.example.f1project.data.remote.ApiConstructorDetailResponse
import com.example.f1project.data.remote.ApiConstructorResultsResponse
import com.example.f1project.data.remote.ApiDriverDetailResponse
import com.example.f1project.data.remote.ApiDriverResultsResponse
import com.example.f1project.data.remote.ApiRaceResponse
import com.example.f1project.data.remote.ApiResponse
import com.example.f1project.data.remote.RetrofitInstance
import com.google.gson.reflect.TypeToken

class F1Repository(private val cacheManager: CacheManager) {

    suspend fun getRaceSchedule(season: String): RepositoryResult<ApiRaceResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getRaceSchedule(season) },
            cacheKey = CacheManager.makeScheduleKey(season),
            type = object : TypeToken<ApiRaceResponse>() {}.type
        )
    }

    suspend fun getDriverStandings(season: String): RepositoryResult<ApiResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getDriverStandings(season) },
            cacheKey = CacheManager.makeDriverStandingsKey(season),
            type = object : TypeToken<ApiResponse>() {}.type
        )
    }

    suspend fun getConstructorStandings(season: String): RepositoryResult<ApiResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getConstructorStandings(season) },
            cacheKey = CacheManager.makeConstructorStandingsKey(season),
            type = object : TypeToken<ApiResponse>() {}.type
        )
    }

    suspend fun getRaceResults(season: String, round: String) =
        RetrofitInstance.api.getRaceResults(season, round)

    suspend fun getQualifyingResults(season: String, round: String) =
        RetrofitInstance.api.getQualifyingResults(season, round)

    suspend fun getSprintResults(season: String, round: String) =
        RetrofitInstance.api.getSprintResults(season, round)

    suspend fun getSprintQualifyingResults(season: String, round: String) =
        RetrofitInstance.api.getSprintQualifyingResults(season, round)

    suspend fun getDriverDetail(driverId: String): RepositoryResult<ApiDriverDetailResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getDriverDetail(driverId) },
            cacheKey = CacheManager.makeDriverDetailKey(driverId),
            type = object : TypeToken<ApiDriverDetailResponse>() {}.type
        )
    }

    suspend fun getDriverSeasonResults(
        season: String,
        driverId: String
    ): RepositoryResult<ApiDriverResultsResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getDriverSeasonResults(season, driverId) },
            cacheKey = CacheManager.makeDriverResultsKey(season, driverId),
            type = object : TypeToken<ApiDriverResultsResponse>() {}.type
        )
    }

    suspend fun getConstructorDetail(
        constructorId: String
    ): RepositoryResult<ApiConstructorDetailResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getConstructorDetail(constructorId) },
            cacheKey = CacheManager.makeConstructorDetailKey(constructorId),
            type = object : TypeToken<ApiConstructorDetailResponse>() {}.type
        )
    }

    suspend fun getConstructorSeasonResults(
        season: String,
        constructorId: String
    ): RepositoryResult<ApiConstructorResultsResponse> {
        return fetchWithCache(
            apiCall = { RetrofitInstance.api.getConstructorSeasonResults(season, constructorId) },
            cacheKey = CacheManager.makeConstructorResultsKey(season, constructorId),
            type = object : TypeToken<ApiConstructorResultsResponse>() {}.type
        )
    }

    private suspend fun <T> fetchWithCache(
        apiCall: suspend () -> T,
        cacheKey: androidx.datastore.preferences.core.Preferences.Key<String>,
        type: java.lang.reflect.Type
    ): RepositoryResult<T> {
        return try {
            val data = apiCall()
            cacheManager.save(cacheKey, data)
            RepositoryResult.Fresh(data)
        } catch (e: Exception) {
            val cached: T? = cacheManager.load(cacheKey, type)
            if (cached != null) {
                RepositoryResult.Cached(cached)
            } else {
                RepositoryResult.Error(e.message ?: "Nieznany błąd")
            }
        }
    }
}

sealed class RepositoryResult<out T> {
    data class Fresh<T>(val data: T) : RepositoryResult<T>()
    data class Cached<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String) : RepositoryResult<Nothing>()
}