package com.example.f1project.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.OpenF1Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DisplayResult(
    val position: String,
    val driverName: String,
    val constructorName: String,
    val constructorId: String,
    val nationality: String,
    val points: String?,
    val time1: String?,
    val time2: String?,
    val time3: String?
)

data class ResultsUiState(
    val title: String = "Wyniki",
    val results: List<DisplayResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ResultsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as F1App).repository
    private val openF1Repository = (application as F1App).openF1Repository

    private val season: String = checkNotNull(savedStateHandle["season"])
    private val round: String = checkNotNull(savedStateHandle["round"])
    private val sessionType: String = checkNotNull(savedStateHandle["sessionType"])
    // DODANE: lokalizacja przekazana przez nawigację
    private val location: String = savedStateHandle["location"] ?: ""

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchResults()
    }

    private fun fetchResults() {
        viewModelScope.launch {
            try {
                when (sessionType) {
                    "race"              -> fetchRaceOrSprintResults(isSprint = false)
                    "sprint"            -> fetchRaceOrSprintResults(isSprint = true)
                    "qualifying"        -> fetchQualifyingResults()
                    "sprint_qualifying" -> fetchSprintQualifyingFromOpenF1()
                    else -> _uiState.value = ResultsUiState(
                        isLoading = false,
                        error = "Nieznany typ sesji: $sessionType"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ResultsUiState(
                    isLoading = false,
                    error = "Błąd pobierania danych: ${e.message}"
                )
            }
        }
    }

    private suspend fun fetchSprintQualifyingFromOpenF1() {
        val year = season.toIntOrNull() ?: run {
            _uiState.value = ResultsUiState(
                isLoading = false,
                error = "Nieprawidłowy format sezonu"
            )
            return
        }
        val roundInt = round.toIntOrNull() ?: run {
            _uiState.value = ResultsUiState(
                isLoading = false,
                error = "Nieprawidłowy format rundy"
            )
            return
        }

        // ZMIANA: przekazujemy lokalizację do repository
        when (val result = openF1Repository.getSprintQualifyingResults(year, roundInt, location)) {
            is OpenF1Result.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val data = result.data as List<DisplayResult>
                _uiState.value = ResultsUiState(
                    title = "Sprint Qualifying — $location",
                    results = data,
                    isLoading = false
                )
            }
            is OpenF1Result.Error -> {
                _uiState.value = ResultsUiState(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    private suspend fun fetchRaceOrSprintResults(isSprint: Boolean) {
        val response = if (isSprint) {
            repository.getSprintResults(season, round)
        } else {
            repository.getRaceResults(season, round)
        }
        val race = response.mrData.raceTable.races?.firstOrNull()
        if (race != null) {
            val resultList = if (isSprint) race.sprintResults else race.results
            val displayResults = (resultList ?: emptyList()).map {
                DisplayResult(
                    position        = it.position ?: "—",
                    driverName      = "${it.driver.givenName ?: ""} ${it.driver.familyName ?: ""}".trim(),
                    constructorName = it.constructor.name ?: "—",
                    constructorId   = it.constructor.constructorId ?: "",
                    nationality     = it.driver.nationality ?: "",
                    points          = it.points,
                    time1           = it.time?.time ?: it.status,
                    time2           = null,
                    time3           = null
                )
            }
            _uiState.value = ResultsUiState(
                title     = race.raceName ?: "Wyniki",
                results   = displayResults,
                isLoading = false
            )
        } else {
            _uiState.value = ResultsUiState(
                isLoading = false,
                error     = "Brak wyników dla tej sesji."
            )
        }
    }

    private suspend fun fetchQualifyingResults() {
        val response = repository.getQualifyingResults(season, round)
        val race = response.mrData.raceTable.races.firstOrNull()
        if (race != null) {
            val displayResults = race.qualifyingResults.map {
                DisplayResult(
                    position        = it.position ?: "—",
                    driverName      = "${it.driver.givenName ?: ""} ${it.driver.familyName ?: ""}".trim(),
                    constructorName = it.constructor.name ?: "—",
                    constructorId   = it.constructor.constructorId ?: "",
                    nationality     = it.driver.nationality ?: "",
                    points          = null,
                    time1           = it.q1,
                    time2           = it.q2,
                    time3           = it.q3
                )
            }
            _uiState.value = ResultsUiState(
                title     = race.raceName ?: "Kwalifikacje",
                results   = displayResults,
                isLoading = false
            )
        } else {
            _uiState.value = ResultsUiState(
                isLoading = false,
                error     = "Brak wyników dla kwalifikacji."
            )
        }
    }
}
