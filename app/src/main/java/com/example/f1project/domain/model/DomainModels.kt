package com.example.f1project.domain.model

// --- Kierowca ---
data class DomainDriver(
    val driverId: String,
    val fullName: String,           // już połączone imię + nazwisko
    val code: String,               // np. "VER"
    val number: String,
    val nationality: String,
    val dateOfBirth: String,
    val wikipediaUrl: String
)

// --- Konstruktor / Zespół ---
data class DomainConstructor(
    val constructorId: String,
    val name: String,
    val nationality: String,
    val wikipediaUrl: String
)

// --- Pozycja w klasyfikacji kierowców ---
data class DomainDriverStanding(
    val position: Int,
    val points: Double,
    val wins: Int,
    val driver: DomainDriver,
    val constructor: DomainConstructor?
)

// --- Pozycja w klasyfikacji konstruktorów ---
data class DomainConstructorStanding(
    val position: Int,
    val points: Double,
    val wins: Int,
    val constructor: DomainConstructor
)

// --- Wyścig w kalendarzu ---
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

// --- Sesja (trening, kwalifikacje, wyścig) ---
data class DomainSession(
    val name: String,
    val date: String?,
    val time: String?,
    val isRace: Boolean = false
)

// --- Wynik wyścigu / kwalifikacji ---
data class DomainResult(
    val position: Int,
    val driverFullName: String,
    val constructorName: String,
    val constructorId: String,
    val nationality: String,
    val points: Double?,
    val timeOrStatus: String?,  // czas wyścigu lub status np. "DNF"
    val q1: String?,            // null jeśli nie kwalifikacje
    val q2: String?,
    val q3: String?
)

// --- Profil kierowcy (szczegóły) ---
data class DomainDriverProfile(
    val driver: DomainDriver,
    val currentTeam: String,
    val seasonStats: DomainSeasonStats,
    val raceResults: List<DomainRaceResult>
)

// --- Profil konstruktora (szczegóły) ---
data class DomainConstructorProfile(
    val constructor: DomainConstructor,
    val seasonStats: DomainSeasonStats,
    val raceResults: List<DomainConstructorRaceResult>
)

// --- Statystyki sezonu ---
data class DomainSeasonStats(
    val points: Int,
    val wins: Int,
    val podiums: Int
)

// --- Wynik wyścigu kierowcy ---
data class DomainRaceResult(
    val round: Int,
    val raceName: String,
    val position: Int,
    val points: Double,
    val gridPosition: Int,
    val status: String
)

// --- Wynik wyścigu konstruktora ---
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