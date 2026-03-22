package com.example.f1project.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1project.data.F1Repository
import com.example.f1project.data.RepositoryResult
import com.example.f1project.domain.mapper.RaceMapper
import com.example.f1project.domain.model.DomainRace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CalendarUiState(
    val races: List<DomainRace> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFromCache: Boolean = false,
    val error: String? = null
)

class CalendarViewModel(
    private val repository: F1Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSeason(season: String) { fetchSchedule(season) }
    fun refresh(season: String) { fetchSchedule(season, isRefresh = true) }

    private fun fetchSchedule(season: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            when (val result = repository.getRaceSchedule(season)) {
                is RepositoryResult.Fresh -> _uiState.value = CalendarUiState(
                    races = RaceMapper.mapList(result.data.mrData.raceTable.races),
                    isLoading = false,
                    isRefreshing = false,
                    isFromCache = false
                )
                is RepositoryResult.Cached -> _uiState.value = CalendarUiState(
                    races = RaceMapper.mapList(result.data.mrData.raceTable.races),
                    isLoading = false,
                    isRefreshing = false,
                    isFromCache = true
                )
                is RepositoryResult.Error -> _uiState.value = CalendarUiState(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Błąd: ${result.message}"
                )
            }
        }
    }

    companion object {
        fun factory(repository: F1Repository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CalendarViewModel(repository) as T
                }
            }
        }
    }
}

fun formatUtcDateTime(date: String?, time: String?): String {
    if (date == null || time == null) return "Brak danych"
    return try {
        val utcDateTime = ZonedDateTime.parse("${date}T${time}")
        val polandDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Europe/Warsaw"))
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM, HH:mm", Locale("pl", "PL"))
        polandDateTime.format(formatter)
    } catch (e: Exception) {
        "$date $time (UTC)"
    }
}