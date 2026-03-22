package com.example.f1project.data.remote

import com.google.gson.annotations.SerializedName

// --- Szczegóły kierowcy ---
data class DriverDetail(
    val driverId: String,
    val givenName: String,
    val familyName: String,
    val dateOfBirth: String?,
    val nationality: String,
    @SerializedName("permanentNumber")
    val permanentNumber: String?,
    val code: String?,
    val url: String?         // link do Wikipedii
)

data class DriverDetailTable(
    @SerializedName("Drivers")
    val drivers: List<DriverDetail>
)

data class MRDataDriverDetail(
    @SerializedName("DriverTable")
    val driverTable: DriverDetailTable
)

data class ApiDriverDetailResponse(
    @SerializedName("MRData")
    val mrData: MRDataDriverDetail
)

// --- Wyniki kierowcy w sezonie ---
data class DriverSeasonResult(
    val round: String,
    val raceName: String,
    @SerializedName("Results")
    val results: List<RaceResult>?
)

data class DriverResultsTable(
    val season: String,
    @SerializedName("Races")
    val races: List<DriverSeasonResult>
)

data class MRDataDriverResults(
    @SerializedName("RaceTable")
    val raceTable: DriverResultsTable
)

data class ApiDriverResultsResponse(
    @SerializedName("MRData")
    val mrData: MRDataDriverResults
)

// --- Szczegóły konstruktora ---
data class ConstructorDetail(
    val constructorId: String,
    val name: String,
    val nationality: String,
    val url: String?
)

data class ConstructorDetailTable(
    @SerializedName("Constructors")
    val constructors: List<ConstructorDetail>
)

data class MRDataConstructorDetail(
    @SerializedName("ConstructorTable")
    val constructorTable: ConstructorDetailTable
)

data class ApiConstructorDetailResponse(
    @SerializedName("MRData")
    val mrData: MRDataConstructorDetail
)

// --- Wyniki konstruktora w sezonie ---
data class ConstructorSeasonResult(
    val round: String,
    val raceName: String,
    @SerializedName("Results")
    val results: List<RaceResult>?
)

data class ConstructorResultsTable(
    val season: String,
    @SerializedName("Races")
    val races: List<ConstructorSeasonResult>
)

data class MRDataConstructorResults(
    @SerializedName("RaceTable")
    val raceTable: ConstructorResultsTable
)

data class ApiConstructorResultsResponse(
    @SerializedName("MRData")
    val mrData: MRDataConstructorResults
)