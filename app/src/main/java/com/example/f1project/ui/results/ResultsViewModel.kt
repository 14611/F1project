package com.example.f1project.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1project.data.F1Repository
import com.example.f1project.data.OpenF1Repository
import com.example.f1project.data.OpenF1Result
import com.example.f1project.domain.mapper.ResultsMapper
import com.example.f1project.domain.model.DomainResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultsUiState(
    val title: String = "Wyniki",
    val results: List<DomainResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ResultsViewModel(
    private val repository: F1Repository,
    private val openF1Repository: OpenF1Repository,
    private val season: String,
    private val round: String,
    private val sessionType: String,
    private val location: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState = _uiState.asStateFlow()

    init { fetchResults() }

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

    private suspend fun fetchRaceOrSprintResults(isSprint: Boolean) {
        val response = if (isSprint) {
            repository.getSprintResults(season, round)
        } else {
            repository.getRaceResults(season, round)
        }
        val race = response.mrData.raceTable.races?.firstOrNull()
        if (race != null) {
            val resultList = if (isSprint) race.sprintResults else race.results
            val domainResults = ResultsMapper.mapRaceResultList(resultList ?: emptyList())
            _uiState.value = ResultsUiState(
                title = race.raceName ?: "Wyniki",
                results = domainResults,
                isLoading = false
            )
        } else {
            _uiState.value = ResultsUiState(
                isLoading = false,
                error = "Brak wyników dla tej sesji."
            )
        }
    }

    private suspend fun fetchQualifyingResults() {
        val response = repository.getQualifyingResults(season, round)
        val race = response.mrData.raceTable.races.firstOrNull()
        if (race != null) {
            val domainResults = ResultsMapper.mapQualifyingResultList(race.qualifyingResults)
            _uiState.value = ResultsUiState(
                title = race.raceName ?: "Kwalifikacje",
                results = domainResults,
                isLoading = false
            )
        } else {
            _uiState.value = ResultsUiState(
                isLoading = false,
                error = "Brak wyników dla kwalifikacji."
            )
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

        when (val result = openF1Repository.getSprintQualifyingResults(year, roundInt, location)) {
            is OpenF1Result.Success -> {
                _uiState.value = ResultsUiState(
                    title = "Sprint Qualifying — $location",
                    results = result.data,
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

    class Factory(
        private val repository: F1Repository,
        private val openF1Repository: OpenF1Repository,
        private val season: String,
        private val round: String,
        private val sessionType: String,
        private val location: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResultsViewModel(
                repository, openF1Repository, season, round, sessionType, location
            ) as T
        }
    }
}