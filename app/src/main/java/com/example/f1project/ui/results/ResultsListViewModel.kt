package com.example.f1project.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1project.data.F1Repository
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.Race
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

data class ResultsListUiState(
    val finishedRaces: List<Race> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFromCache: Boolean = false,
    val error: String? = null
)

class ResultsListViewModel(
    private val repository: F1Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsListUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSeason(season: String) { fetchFinishedRaces(season) }
    fun refresh(season: String) { fetchFinishedRaces(season, isRefresh = true) }

    private fun fetchFinishedRaces(season: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            when (val result = repository.getRaceSchedule(season)) {
                is RepositoryResult.Fresh -> _uiState.value = ResultsListUiState(
                    finishedRaces = filterFinished(result.data.mrData.raceTable.races, season),
                    isLoading = false,
                    isRefreshing = false,
                    isFromCache = false
                )
                is RepositoryResult.Cached -> _uiState.value = ResultsListUiState(
                    finishedRaces = filterFinished(result.data.mrData.raceTable.races, season),
                    isLoading = false,
                    isRefreshing = false,
                    isFromCache = true
                )
                is RepositoryResult.Error -> _uiState.value = ResultsListUiState(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Brak danych i brak połączenia z internetem"
                )
            }
        }
    }

    private fun filterFinished(races: List<Race>, season: String): List<Race> {
        val currentYear = LocalDate.now().year.toString()
        return if (season == currentYear) {
            // ZMIANA: wyścig pojawia się gdy jakakolwiek jego sesja się skończyła
            races.filter { it.hasAnyFinishedSession() }.reversed()
        } else {
            races.reversed()
        }
    }

    // ZMIANA: sprawdzamy czy jakakolwiek sesja wyścigu już się zakończyła
    // Dzięki temu np. wyniki kwalifikacji pojawiają się od razu po kwalifikacjach,
    // nie dopiero po zakończeniu wyścigu głównego
    private fun Race.hasAnyFinishedSession(): Boolean {
        val now = ZonedDateTime.now()

        // Zbieramy wszystkie sesje które mają datę i czas
        val allSessions = listOfNotNull(
            parseDatetime(firstPractice?.date, firstPractice?.time),
            parseDatetime(secondPractice?.date, secondPractice?.time),
            parseDatetime(thirdPractice?.date, thirdPractice?.time),
            parseDatetime(sprintQualifying?.date, sprintQualifying?.time),
            parseDatetime(sprint?.date, sprint?.time),
            parseDatetime(qualifying?.date, qualifying?.time),
            parseDatetime(date, time)  // wyścig główny
        )

        // Wystarczy że jedna sesja się skończyła (+ 2h bufor na zakończenie)
        return allSessions.any { sessionTime ->
            sessionTime.plusHours(2).isBefore(now)
        }
    }

    private fun parseDatetime(date: String?, time: String?): ZonedDateTime? {
        if (date == null || time == null) return null
        return try {
            ZonedDateTime.parse("${date}T${time}")
        } catch (e: DateTimeParseException) {
            null
        }
    }

    companion object {
        fun factory(repository: F1Repository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ResultsListViewModel(repository) as T
                }
            }
        }
    }
}