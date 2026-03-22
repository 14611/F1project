package com.example.f1project.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface F1ApiService {

    @GET("f1/{season}/driverstandings.json")
    suspend fun getDriverStandings(@Path("season") season: String): ApiResponse

    @GET("f1/{season}/constructorstandings.json")
    suspend fun getConstructorStandings(@Path("season") season: String): ApiResponse

    @GET("f1/{season}.json")
    suspend fun getRaceSchedule(@Path("season") season: String): ApiRaceResponse

    @GET("f1/{season}/{round}/results.json")
    suspend fun getRaceResults(
        @Path("season") season: String,
        @Path("round") round: String
    ): ApiResultsResponse

    @GET("f1/{season}/{round}/qualifying.json")
    suspend fun getQualifyingResults(
        @Path("season") season: String,
        @Path("round") round: String
    ): ApiQualifyingResponse

    @GET("f1/{season}/{round}/sprint.json")
    suspend fun getSprintResults(
        @Path("season") season: String,
        @Path("round") round: String
    ): ApiResultsResponse

    @GET("f1/{season}/{round}/sprint_qualifying.json")
    suspend fun getSprintQualifyingResults(
        @Path("season") season: String,
        @Path("round") round: String
    ): ApiQualifyingResponse

    // DODANE: szczegóły kierowcy
    @GET("f1/drivers/{driverId}.json")
    suspend fun getDriverDetail(
        @Path("driverId") driverId: String
    ): ApiDriverDetailResponse

    // DODANE: wyniki kierowcy w sezonie
    @GET("f1/{season}/drivers/{driverId}/results.json?limit=100")
    suspend fun getDriverSeasonResults(
        @Path("season") season: String,
        @Path("driverId") driverId: String
    ): ApiDriverResultsResponse

    // DODANE: szczegóły konstruktora
    @GET("f1/constructors/{constructorId}.json")
    suspend fun getConstructorDetail(
        @Path("constructorId") constructorId: String
    ): ApiConstructorDetailResponse

    // DODANE: wyniki konstruktora w sezonie
    @GET("f1/{season}/constructors/{constructorId}/results.json?limit=100")
    suspend fun getConstructorSeasonResults(
        @Path("season") season: String,
        @Path("constructorId") constructorId: String
    ): ApiConstructorResultsResponse

    companion object {
        const val BASE_URL = "https://api.jolpi.ca/ergast/"
    }
}