package com.example.f1project.domain.model

data class DomainDriver(
    val driverId: String,
    val fullName: String,
    val code: String,
    val number: String,
    val nationality: String,
    val dateOfBirth: String,
    val wikipediaUrl: String
)

data class DomainConstructor(
    val constructorId: String,
    val name: String,
    val nationality: String,
    val wikipediaUrl: String
)

data class DomainDriverStanding(
    val position: Int,
    val points: Double,
    val wins: Int,
    val driver: DomainDriver,
    val constructor: DomainConstructor?
)

data class DomainConstructorStanding(
    val position: Int,
    val points: Double,
    val wins: Int,
    val constructor: DomainConstructor
)

data class DomainRace(
    val season: String,
    val round: Int,
    val name: String,
    val circuitName: String,
    val country: String,
    val locality: String,
    val raceDate: String?,
    val raceTime: String?,
    val sessions: List<DomainSession>
)

data class DomainSession(
    val name: String,
    val date: String?,
    val time: String?,
    val isRace: Boolean = false
)

data class DomainResult(
    val position: Int,
    val driverFullName: String,
    val constructorName: String,
    val constructorId: String,
    val nationality: String,
    val points: Double?,
    val timeOrStatus: String?,
    val q1: String?,
    val q2: String?,
    val q3: String?
)

data class DomainDriverProfile(
    val driver: DomainDriver,
    val currentTeam: String,
    val seasonStats: DomainSeasonStats,
    val raceResults: List<DomainRaceResult>
)

data class DomainConstructorProfile(
    val constructor: DomainConstructor,
    val seasonStats: DomainSeasonStats,
    val raceResults: List<DomainConstructorRaceResult>
)

// ZMIANA: points zmienione z Int na Double
// Powód: F1 przyznaje 0.5 punktu za najszybsze okrążenie oraz
// historycznie stosowało punkty ułamkowe (np. sezon 1961 — 0.5 pkt)
// Używanie Int cicho ucinało ułamki przez .toInt()
data class DomainSeasonStats(
    val points: Double,   // było: Int
    val wins: Int,
    val podiums: Int
)

data class DomainRaceResult(
    val round: Int,
    val raceName: String,
    val position: Int,
    val points: Double,
    val gridPosition: Int,
    val status: String
)

data class DomainConstructorRaceResult(
    val round: Int,
    val raceName: String,
    val driver1: DomainDriverResult,
    val driver2: DomainDriverResult?
)

data class DomainDriverResult(
    val fullName: String,
    val position: Int,
    val points: Double
)