package com.example.f1project.data

import android.content.Context
import com.example.f1project.data.remote.ApiConstructorDetailResponse
import com.example.f1project.data.remote.ApiConstructorResultsResponse
import com.example.f1project.data.remote.ApiDriverDetailResponse
import com.example.f1project.data.remote.ApiDriverResultsResponse
import com.example.f1project.data.remote.ApiQualifyingResponse
import com.example.f1project.data.remote.ApiRaceResponse
import com.example.f1project.data.remote.ApiResponse
import com.example.f1project.data.remote.ApiResultsResponse
import com.example.f1project.data.remote.F1ApiService
import com.example.f1project.data.remote.NetworkUtils
import com.example.f1project.data.remote.RetrofitInstance

class F1Repository(private val context: Context) {

    // cacheOnly=true  → apiCacheOnly  — dysk, nigdy sieć (widget)
    // forceRefresh=true → apiRefresh — zawsze sieć (pull-to-refresh)
    // domyślnie        → api        — sieć jeśli cache > 5 min

    suspend fun getRaceSchedule(
        season: String,
        forceRefresh: Boolean = false,
        cacheOnly: Boolean = false
    ): RepositoryResult<ApiRaceResponse> =
        fetchWithCache(forceRefresh, cacheOnly) { it.getRaceSchedule(season) }

    suspend fun getDriverStandings(
        season: String,
        forceRefresh: Boolean = false,
        cacheOnly: Boolean = false
    ): RepositoryResult<ApiResponse> =
        fetchWithCache(forceRefresh, cacheOnly) { it.getDriverStandings(season) }

    suspend fun getConstructorStandings(
        season: String,
        forceRefresh: Boolean = false,
        cacheOnly: Boolean = false
    ): RepositoryResult<ApiResponse> =
        fetchWithCache(forceRefresh, cacheOnly) { it.getConstructorStandings(season) }

    suspend fun getRaceResults(
        season: String,
        round: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiResultsResponse> =
        fetchWithCache(forceRefresh) { it.getRaceResults(season, round) }

    suspend fun getQualifyingResults(
        season: String,
        round: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiQualifyingResponse> =
        fetchWithCache(forceRefresh) { it.getQualifyingResults(season, round) }

    suspend fun getSprintResults(
        season: String,
        round: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiResultsResponse> =
        fetchWithCache(forceRefresh) { it.getSprintResults(season, round) }

    suspend fun getSprintQualifyingResults(
        season: String,
        round: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiQualifyingResponse> =
        fetchWithCache(forceRefresh) { it.getSprintQualifyingResults(season, round) }

    suspend fun getDriverDetail(
        driverId: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiDriverDetailResponse> =
        fetchWithCache(forceRefresh) { it.getDriverDetail(driverId) }

    suspend fun getDriverSeasonResults(
        season: String,
        driverId: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiDriverResultsResponse> =
        fetchWithCache(forceRefresh) { it.getDriverSeasonResults(season, driverId) }

    suspend fun getConstructorDetail(
        constructorId: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiConstructorDetailResponse> =
        fetchWithCache(forceRefresh) { it.getConstructorDetail(constructorId) }

    suspend fun getConstructorSeasonResults(
        season: String,
        constructorId: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<ApiConstructorResultsResponse> =
        fetchWithCache(forceRefresh) { it.getConstructorSeasonResults(season, constructorId) }

    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun <T> fetchWithCache(
        forceRefresh: Boolean = false,
        cacheOnly: Boolean = false,
        apiCall: suspend (F1ApiService) -> T
    ): RepositoryResult<T> {
        val service = when {
            cacheOnly    -> RetrofitInstance.apiCacheOnly
            forceRefresh -> RetrofitInstance.apiRefresh
            else         -> RetrofitInstance.api
        }
        val online = NetworkUtils.isOnline(context)
        return try {
            val data = apiCall(service)
            when {
                cacheOnly -> RepositoryResult.Cached(data)
                online    -> RepositoryResult.Fresh(data)
                else      -> RepositoryResult.Cached(data)
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                when {
                    cacheOnly -> "Brak danych w cache"
                    online    -> "Błąd serwera: ${e.message}"
                    else      -> "Brak połączenia i brak danych w cache"
                }
            )
        }
    }
}

sealed class RepositoryResult<out T> {
    data class Fresh<T>(val data: T)      : RepositoryResult<T>()
    data class Cached<T>(val data: T)     : RepositoryResult<T>()
    data class Error(val message: String) : RepositoryResult<Nothing>()
}