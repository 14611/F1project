package com.example.f1project.data.remote

import com.google.gson.annotations.SerializedName

// --- GŁÓWNA STRUKTURA ODPOWIEDZI Z API ---
// To jest punkt wejścia danych z sieci.
data class ApiRaceResponse(
    @SerializedName("MRData") val mrData: MRDataRaces
)

data class MRDataRaces(
    @SerializedName("RaceTable") val raceTable: RaceTable
)

data class RaceTable(
    val season: String,
    @SerializedName("Races") val races: List<Race>
)

// --- MODEL POJEDYNCZEGO WYŚCIGU ---
// Zawiera informacje o rundzie, torze oraz wszystkich sesjach (treningi, kwalifikacje, wyścig).
data class Race(
    val season: String,
    val round: String,
    val raceName: String,
    @SerializedName("Circuit") val circuit: Circuit,
    val date: String?,          // Data głównego wyścigu
    val time: String?,          // Godzina startu
    // Poniżej sesje dodatkowe (mogą być nullem, jeśli dane nie są dostępne)
    @SerializedName("FirstPractice") val firstPractice: Session?,
    @SerializedName("SecondPractice") val secondPractice: Session?,
    @SerializedName("ThirdPractice") val thirdPractice: Session?,
    @SerializedName("Qualifying") val qualifying: Session?,
    @SerializedName("Sprint") val sprint: Session?,
    @SerializedName("SprintQualifying") val sprintQualifying: Session?
)

// --- OBIEKTY POMOCNICZE (TOR, LOKALIZACJA, SESJA) ---
data class Circuit(
    val circuitId: String,
    val circuitName: String,
    @SerializedName("Location") val location: Location
)

data class Location(
    val country: String,
    val locality: String
)

// Uniwersalna klasa dla każdej sesji posiadającej datę i czas
data class Session(
    val date: String?,
    val time: String?
)