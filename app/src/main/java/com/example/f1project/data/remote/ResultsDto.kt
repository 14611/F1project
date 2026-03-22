package com.example.f1project.data.remote

import com.google.gson.annotations.SerializedName

// --- Klasy reprezentujące wyniki wyścigów F1 ---

// Pojedynczy wynik kierowcy w wyścigu
data class RaceResult(
    val number: String, // Numer startowy kierowcy
    val position: String, // Pozycja końcowa w wyścigu
    val points: String, // Punkty zdobyte w wyścigu
    @SerializedName("Driver")
    val driver: Driver, // Informacje o kierowcy
    @SerializedName("Constructor")
    val constructor: Constructor, // Informacje o zespole
    val grid: String, // Pozycja startowa
    val status: String, // Status wyścigu np. "Finished", "+1 Lap"
    @SerializedName("Time")
    val time: RaceTime? // Czas ukończenia wyścigu (może być null)
)

// Czas wyścigu kierowcy
data class RaceTime(
    val millis: String, // Czas w milisekundach
    val time: String // Czas w formacie "mm:ss.SSS"
)

// --- Klasy opakowujące dane API ---

// Pojedynczy wyścig z wynikami
data class RaceWithResults(
    val season: String,
    val round: String,
    val raceName: String,
    @SerializedName("Results")
    val results: List<RaceResult>?, // Lista wyników kierowców (może być null)
    @SerializedName("SprintResults")
    val sprintResults: List<RaceResult>?
)

// Tabela wyników dla sezonu i rundy
data class ResultsTable(
    val season: String,
    val round: String,
    @SerializedName("Races")
    val races: List<RaceWithResults>? // Lista wyścigów w danej rundzie (może być null)
)

// Dane MRData zwracane przez API
data class MRDataResults(
    @SerializedName("RaceTable")
    val raceTable: ResultsTable
)

// Odpowiedź API dla wyników wyścigu
data class ApiResultsResponse(
    @SerializedName("MRData")
    val mrData: MRDataResults
)
