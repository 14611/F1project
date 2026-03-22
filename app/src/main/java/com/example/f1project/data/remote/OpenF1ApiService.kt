package com.example.f1project.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenF1ApiService {

    // DODANE: pobierz wszystkie sesje dla roku — bez filtra nazwy
    @GET("sessions")
    suspend fun getAllSessions(
        @Query("year") year: Int
    ): List<OpenF1Session>

    // Zostaje dla ewentualnego użycia w przyszłości
    @GET("sessions")
    suspend fun getSessions(
        @Query("year") year: Int,
        @Query("session_name") sessionName: String
    ): List<OpenF1Session>

    @GET("laps")
    suspend fun getLaps(
        @Query("session_key") sessionKey: Int
    ): List<OpenF1Lap>

    @GET("drivers")
    suspend fun getDrivers(
        @Query("session_key") sessionKey: Int
    ): List<OpenF1Driver>

    companion object {
        const val BASE_URL = "https://api.openf1.org/v1/"
    }
}