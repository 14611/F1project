package com.example.f1project.data.remote
import com.google.gson.annotations.SerializedName
// -----------------------------------
// Klasy modelu danych dla klasyfikacji kierowców i konstruktorów F1
// -----------------------------------

// Dane kierowcy
data class Driver(
    val driverId: String?,      // Unikalny identyfikator kierowcy
    val givenName: String?,     // Imię
    val familyName: String?,    // Nazwisko
    val code: String?,          // Kod kierowcy (np. HAM)
    val nationality: String?    // Narodowość
)

// Pozycja kierowcy w klasyfikacji
data class DriverStanding(
    val position: String?,      // Pozycja w rankingu
    val points: String?,        // Punkty zdobyte
    val wins: String?,          // Liczba wygranych wyścigów
    @SerializedName("Driver")
    val driver: Driver,        // Informacje o kierowcy
    @SerializedName("Constructors")
    val constructors: List<Constructor> // Konstruktorzy kierowcy
)

// -----------------------------------
// Dane konstruktora
// -----------------------------------

data class Constructor(
    val constructorId: String?, // Unikalny identyfikator konstruktora
    val name: String?,          // Nazwa zespołu
    val nationality: String ?   // Narodowość zespołu
)

// Pozycja konstruktora w klasyfikacji
data class ConstructorStanding(
    val position: String?,      // Pozycja w rankingu
    val points: String?,        // Punkty zdobyte
    val wins: String?,          // Liczba wygranych wyścigów
    @SerializedName("Constructor")
    val constructor: Constructor // Informacje o konstruktorze
)

// -----------------------------------
// Struktura danych odpowiedzi API
// -----------------------------------

data class StandingsList(
    val season: String,                        // Sezon F1
    @SerializedName("DriverStandings")
    val driverStandings: List<DriverStanding>?,       // Lista klasyfikacji kierowców (opcjonalna)
    @SerializedName("ConstructorStandings")
    val constructorStandings: List<ConstructorStanding>? // Lista klasyfikacji konstruktorów (opcjonalna)
)

data class StandingsTable(
    @SerializedName("StandingsLists")
    val standingsLists: List<StandingsList> // Lista zestawów klasyfikacji w sezonie
)

data class MRData(
    @SerializedName("StandingsTable")
    val standingsTable: StandingsTable // Tabela klasyfikacji
)

data class ApiResponse(
    @SerializedName("MRData")
    val mrData: MRData // Główny obiekt odpowiedzi API
)
