package com.example.f1project.ui.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1project.data.F1Repository
import com.example.f1project.data.RepositoryResult
import com.example.f1project.domain.mapper.StandingsMapper
import com.example.f1project.domain.model.DomainConstructorStanding
import com.example.f1project.domain.model.DomainDriverStanding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StandingsUiState(
    val drivers: List<DomainDriverStanding> = emptyList(),
    val constructors: List<DomainConstructorStanding> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFromCache: Boolean = false,
    val error: String? = null
)

class StandingsViewModel(
    private val repository: F1Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSeason(season: String) { fetchStandings(season) }
    fun refresh(season: String) { fetchStandings(season, isRefresh = true) }

    private fun fetchStandings(season: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            val driverResult = repository.getDriverStandings(season, forceRefresh = isRefresh)
            val constructorResult = repository.getConstructorStandings(season, forceRefresh = isRefresh)

            val drivers = when (driverResult) {
                is RepositoryResult.Fresh -> StandingsMapper.mapDriverList(
                    driverResult.data.mrData.standingsTable
                        .standingsLists.firstOrNull()?.driverStandings ?: emptyList()
                )
                is RepositoryResult.Cached -> StandingsMapper.mapDriverList(
                    driverResult.data.mrData.standingsTable
                        .standingsLists.firstOrNull()?.driverStandings ?: emptyList()
                )
                is RepositoryResult.Error -> emptyList()
            }

            val constructors = when (constructorResult) {
                is RepositoryResult.Fresh -> StandingsMapper.mapConstructorList(
                    constructorResult.data.mrData.standingsTable
                        .standingsLists.firstOrNull()?.constructorStandings ?: emptyList()
                )
                is RepositoryResult.Cached -> StandingsMapper.mapConstructorList(
                    constructorResult.data.mrData.standingsTable
                        .standingsLists.firstOrNull()?.constructorStandings ?: emptyList()
                )
                is RepositoryResult.Error -> emptyList()
            }

            val isFromCache = driverResult is RepositoryResult.Cached
                    || constructorResult is RepositoryResult.Cached

            val error = if (driverResult is RepositoryResult.Error
                && constructorResult is RepositoryResult.Error
            ) "Brak danych i brak połączenia z internetem" else null

            _uiState.value = StandingsUiState(
                drivers = drivers,
                constructors = constructors,
                isLoading = false,
                isRefreshing = false,
                isFromCache = isFromCache,
                error = error
            )
        }
    }

    companion object {
        fun factory(repository: F1Repository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StandingsViewModel(repository) as T
                }
            }
        }
    }
}