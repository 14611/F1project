package com.example.f1project.ui.season

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class SeasonViewModel : ViewModel() {

    // Zakres dostępnych sezonów — Ergast ma dane od 1950, ale realnie od ~2000
    val availableSeasons = (2000..LocalDate.now().year).map { it.toString() }.reversed()

    private val _selectedSeason = MutableStateFlow(LocalDate.now().year.toString())
    val selectedSeason = _selectedSeason.asStateFlow()

    fun selectSeason(season: String) {
        _selectedSeason.value = season
    }
}