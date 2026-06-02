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

// ZMIANA: F1ApiService wstrzykiwane przez konstruktor zamiast pobierania ze statycznego singletona
//
// Zalety:
//   1. Dependency Inversion — Repository zależy od interfejsu F1ApiService, nie od RetrofitInstance
//   2. Testowalność — w testach jednostkowych wystarczy przekazać mockk<F1ApiService>()
//      zamiast mockować statyczny obiekt
//   3. Gotowość na Hilt/Koin — DI framework wstrzyknie implementacje automatycznie
//
// Trzy osobne instancje dla różnych strategii cache:
//   normalApi    — OkHttp waliduje max-age (5 min)
//   refreshApi   — FORCE_NETWORK, omija cache
//   cacheOnlyApi — only-if-cached, nigdy nie wychodzi do sieci (widget)
class F1Repository(
    private val context: Context,
    private val normalApi: F1ApiService,
    private val refreshApi: F1ApiService,
    private val cacheOnlyApi: F1ApiService
) {

    suspend fun getRaceSchedule(
        season: String,
        forceRefresh: Boolean = false,
        cacheOnly: Boolean    = false
    ): RepositoryResult<ApiRaceResponse> =
        fetchWithCache(forceRefresh, cacheOnly) { it.getRaceSchedule(season) }

    suspend fun getDriverStandings(
        season: String,
        forceRefresh: Boolean = false,
        cacheOnly: Boolean    = false
    ): RepositoryResult<ApiResponse> =
        fetchWithCache(forceRefresh, cacheOnly) { it.getDriverStandings(season) }

    suspend fun getConstructorStandings(
        season: String,
        forceRefresh: Boolean = false,
        cacheOnly: Boolean    = false
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
    // ZMIANA: wybiera właściwy serwis na podstawie flag,
    // zamiast odwoływać się bezpośrednio do RetrofitInstance
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun <T> fetchWithCache(
        forceRefresh: Boolean = false,
        cacheOnly: Boolean    = false,
        apiCall: suspend (F1ApiService) -> T
    ): RepositoryResult<T> {
        val service = when {
            cacheOnly    -> cacheOnlyApi
            forceRefresh -> refreshApi
            else         -> normalApi
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
    data class Fresh<T>(val data: T)            : RepositoryResult<T>()
    data class Cached<T>(val data: T)           : RepositoryResult<T>()
    data class Error(val message: String)        : RepositoryResult<Nothing>()
}