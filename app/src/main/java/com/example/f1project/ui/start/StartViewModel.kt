package com.example.f1project.ui.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.Race
import com.example.f1project.data.remote.Session
import com.example.f1project.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

data class UpcomingSession(
    val raceName: String,
    val sessionName: String,
    val dateTime: ZonedDateTime
)

data class StartUiState(
    val nextRaceSession: UpcomingSession?     = null,
    val nextUpcomingSession: UpcomingSession? = null,
    val raceCountdown: String                 = "...",
    val sessionCountdown: String              = "...",
    val isLoading: Boolean                    = true,
    val isRefreshing: Boolean                 = false,
    val isFromCache: Boolean                  = false,
    val error: String?                        = null
)

class StartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as F1App).repository
    private val _uiState   = MutableStateFlow(StartUiState())
    val uiState            = _uiState.asStateFlow()
    private var countdownJob: Job? = null

    init { loadSchedule() }

    fun refresh() { loadSchedule(isRefresh = true) }

    private fun loadSchedule(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            val currentSeason = java.time.LocalDate.now().year.toString()
            when (val result = repository.getRaceSchedule(
                season       = currentSeason,
                forceRefresh = isRefresh
            )) {
                is RepositoryResult.Fresh  ->
                    processSchedule(result.data.mrData.raceTable.races, isFromCache = false)
                is RepositoryResult.Cached ->
                    processSchedule(result.data.mrData.raceTable.races, isFromCache = true)
                is RepositoryResult.Error  ->
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        isRefreshing = false,
                        error        = "Brak połączenia z internetem"
                    )
            }
        }
    }

    private fun processSchedule(races: List<Race>, isFromCache: Boolean) {
        val now               = ZonedDateTime.now()
        val allFutureSessions = races
            .flatMap  { race -> getAllSessionsFromRace(race) }
            .filter   { it.dateTime.isAfter(now) }
            .sortedBy { it.dateTime }

        _uiState.value = _uiState.value.copy(
            nextUpcomingSession = allFutureSessions.firstOrNull(),
            nextRaceSession     = allFutureSessions.firstOrNull { it.sessionName == "Wyścig" },
            isLoading           = false,
            isRefreshing        = false,
            isFromCache         = isFromCache
        )

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                _uiState.value = state.copy(
                    raceCountdown    = TimeFormatter.formatCountdownDetailed(
                        state.nextRaceSession?.dateTime
                    ),
                    sessionCountdown = TimeFormatter.formatCountdownDetailed(
                        state.nextUpcomingSession?.dateTime
                    )
                )
                delay(1000)
            }
        }
    }

    private fun getAllSessionsFromRace(race: Race): List<UpcomingSession> =
        listOfNotNull(
            createSession(race, race.firstPractice,    "Trening 1"),
            createSession(race, race.secondPractice,   "Trening 2"),
            createSession(race, race.thirdPractice,    "Trening 3"),
            createSession(race, race.qualifying,       "Kwalifikacje"),
            createSession(race, race.sprint,           "Sprint"),
            createSession(race, race.sprintQualifying, "Sprint Kwal."),
            parseRace(race)
        )

    private fun createSession(race: Race, session: Session?, name: String): UpcomingSession? {
        val date = session?.date ?: return null
        val time = session.time  ?: return null
        return parseSession(race.raceName, name, date, time)
    }

    private fun parseRace(race: Race): UpcomingSession? {
        val date = race.date ?: return null
        val time = race.time ?: return null
        return parseSession(race.raceName, "Wyścig", date, time)
    }

    private fun parseSession(
        raceName: String,
        sessionName: String,
        date: String,
        time: String
    ): UpcomingSession? = try {
        UpcomingSession(raceName, sessionName, ZonedDateTime.parse("${date}T${time}"))
    } catch (_: DateTimeParseException) { null }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}