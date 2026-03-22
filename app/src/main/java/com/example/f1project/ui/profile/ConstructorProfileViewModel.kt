package com.example.f1project.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.domain.mapper.ProfileMapper
import com.example.f1project.domain.model.DomainConstructorProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConstructorProfileUiState(
    val profile: DomainConstructorProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ConstructorProfileViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as F1App).repository
    private val constructorId: String = checkNotNull(savedStateHandle["constructorId"])
    private val season: String = checkNotNull(savedStateHandle["season"])

    private val _uiState = MutableStateFlow(ConstructorProfileUiState())
    val uiState = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ConstructorProfileUiState(isLoading = true)

            val detailDeferred = async { repository.getConstructorDetail(constructorId) }
            val resultsDeferred = async {
                repository.getConstructorSeasonResults(season, constructorId)
            }

            val detail = when (val r = detailDeferred.await()) {
                is RepositoryResult.Fresh ->
                    r.data.mrData.constructorTable.constructors.firstOrNull()
                is RepositoryResult.Cached ->
                    r.data.mrData.constructorTable.constructors.firstOrNull()
                is RepositoryResult.Error -> null
            }

            val races = when (val r = resultsDeferred.await()) {
                is RepositoryResult.Fresh -> r.data.mrData.raceTable.races
                is RepositoryResult.Cached -> r.data.mrData.raceTable.races
                is RepositoryResult.Error -> emptyList()
            }

            if (detail == null) {
                _uiState.value = ConstructorProfileUiState(
                    isLoading = false,
                    error = "Nie znaleziono danych zespołu"
                )
                return@launch
            }

            val profile = ProfileMapper.mapConstructorProfile(detail, races)
            _uiState.value = ConstructorProfileUiState(profile = profile, isLoading = false)
        }
    }
}