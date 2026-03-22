package com.example.f1project.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.domain.mapper.ProfileMapper
import com.example.f1project.domain.model.DomainDriverProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverProfileUiState(
    val profile: DomainDriverProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DriverProfileViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as F1App).repository
    private val driverId: String = checkNotNull(savedStateHandle["driverId"])
    private val season: String = checkNotNull(savedStateHandle["season"])

    private val _uiState = MutableStateFlow(DriverProfileUiState())
    val uiState = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = DriverProfileUiState(isLoading = true)

            val detailDeferred = async { repository.getDriverDetail(driverId) }
            val resultsDeferred = async { repository.getDriverSeasonResults(season, driverId) }
            val standingsDeferred = async { repository.getDriverStandings(season) }

            val detail = when (val r = detailDeferred.await()) {
                is RepositoryResult.Fresh -> r.data.mrData.driverTable.drivers.firstOrNull()
                is RepositoryResult.Cached -> r.data.mrData.driverTable.drivers.firstOrNull()
                is RepositoryResult.Error -> null
            }

            val races = when (val r = resultsDeferred.await()) {
                is RepositoryResult.Fresh -> r.data.mrData.raceTable.races
                is RepositoryResult.Cached -> r.data.mrData.raceTable.races
                is RepositoryResult.Error -> emptyList()
            }

            val team = when (val r = standingsDeferred.await()) {
                is RepositoryResult.Fresh -> r.data.mrData.standingsTable
                    .standingsLists.firstOrNull()?.driverStandings
                    ?.firstOrNull { it.driver.driverId == driverId }
                    ?.constructors?.firstOrNull()?.name ?: ""
                is RepositoryResult.Cached -> r.data.mrData.standingsTable
                    .standingsLists.firstOrNull()?.driverStandings
                    ?.firstOrNull { it.driver.driverId == driverId }
                    ?.constructors?.firstOrNull()?.name ?: ""
                is RepositoryResult.Error -> ""
            }

            if (detail == null) {
                _uiState.value = DriverProfileUiState(
                    isLoading = false,
                    error = "Nie znaleziono danych kierowcy"
                )
                return@launch
            }

            val profile = ProfileMapper.mapDriverProfile(detail, team, races)
            _uiState.value = DriverProfileUiState(profile = profile, isLoading = false)
        }
    }
}