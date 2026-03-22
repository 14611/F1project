package com.example.f1project.data.remote

import com.google.gson.annotations.SerializedName

data class OpenF1Session(
    @SerializedName("session_key")
    val sessionKey: Int,
    @SerializedName("session_name")
    val sessionName: String,
    @SerializedName("year")
    val year: Int,
    @SerializedName("location")
    val location: String?,
    @SerializedName("country_name")
    val countryName: String?,
    @SerializedName("circuit_short_name")
    val circuitShortName: String?
)

data class OpenF1Lap(
    @SerializedName("driver_number")
    val driverNumber: Int,
    @SerializedName("lap_duration")
    val lapDuration: Double?,
    @SerializedName("lap_number")
    val lapNumber: Int,
    @SerializedName("session_key")
    val sessionKey: Int,
    @SerializedName("is_pit_out_lap")
    val isPitOutLap: Boolean?,
    // DODANE: czas startu okrążenia do podziału na segmenty
    @SerializedName("date_start")
    val dateStart: String?
)

data class OpenF1Driver(
    @SerializedName("driver_number")
    val driverNumber: Int,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("name_acronym")
    val nameAcronym: String,
    @SerializedName("team_name")
    val teamName: String?,
    @SerializedName("team_colour")
    val teamColour: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("session_key")
    val sessionKey: Int
)