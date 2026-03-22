package com.example.f1project.data.remote



import com.google.gson.annotations.SerializedName



// --- Klasy dla wyników kwalifikacji ---



// Pojedynczy wynik kwalifikacji kierowcy

data class QualifyingResult(

    val position: String, // Pozycja kierowcy w kwalifikacjach

    @SerializedName("Driver")

    val driver: Driver, // Dane kierowcy

    @SerializedName("Constructor")

    val constructor: Constructor, // Dane zespołu

    @SerializedName("Q1")

    val q1: String?, // Czas w sesji Q1 (może być null)

    @SerializedName("Q2")

    val q2: String?, // Czas w sesji Q2 (może być null)

    @SerializedName("Q3")

    val q3: String? // Czas w sesji Q3 (może być null)

)



// Wyścig wraz z listą wyników kwalifikacji

data class RaceWithQualifyingResults(

    val raceName: String,

    @SerializedName("QualifyingResults")

    val qualifyingResults: List<QualifyingResult>

)



// Tabela kwalifikacji (zawiera listę wyścigów)

data class QualifyingTable(

    @SerializedName("Races")

    val races: List<RaceWithQualifyingResults>

)



// Opakowanie danych MRData dla kwalifikacji

data class MRDataQualifying(

    @SerializedName("RaceTable")

    val raceTable: QualifyingTable

)



// Główna odpowiedź API dla kwalifikacji

data class ApiQualifyingResponse(

    @SerializedName("MRData")

    val mrData: MRDataQualifying

)