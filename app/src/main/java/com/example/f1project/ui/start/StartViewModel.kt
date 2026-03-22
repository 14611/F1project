package com.example.f1project.ui.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.f1project.F1App
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.Race
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

data class UpcomingSession(
    val raceName: String,
    val sessionName: String,
    val dateTime: ZonedDateTime
)

data class StartUiState(
    val nextRaceSession: UpcomingSession? = null,
    val nextUpcomingSession: UpcomingSession? = null,
    val raceCountdown: String = "...",
    val sessionCountdown: String = "...",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isFromCache: Boolean = false,
    val error: String? = null
)

class StartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as F1App).repository

    private val _uiState = MutableStateFlow(StartUiState())
    val uiState = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadSchedule()
    }

    fun refresh() {
        loadSchedule(isRefresh = true)
    }

    private fun loadSchedule(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            val currentSeason = java.time.LocalDate.now().year.toString()
            when (val result = repository.getRaceSchedule(currentSeason)) {
                is RepositoryResult.Fresh -> processSchedule(result.data.mrData.raceTable.races, isFromCache = false)
                is RepositoryResult.Cached -> processSchedule(result.data.mrData.raceTable.races, isFromCache = true)
                is RepositoryResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Brak połączenia z internetem"
                    )
                }
            }
        }
    }

    private fun processSchedule(races: List<Race>, isFromCache: Boolean) {
        val now = ZonedDateTime.now()
        val allFutureSessions = races
            .flatMap { race -> getAllSessionsFromRace(race) }
            .filter { it.dateTime.isAfter(now) }
            .sortedBy { it.dateTime }

        _uiState.value = _uiState.value.copy(
            nextUpcomingSession = allFutureSessions.firstOrNull(),
            nextRaceSession = allFutureSessions.find { it.sessionName == "Wyścig" },
            isLoading = false,
            isRefreshing = false,
            isFromCache = isFromCache
        )

        startCountdown()
    }

    private fun getAllSessionsFromRace(race: Race): List<UpcomingSession> {
        return listOfNotNull(
            createSession(race, race.firstPractice, "Trening 1"),
            createSession(race, race.secondPractice, "Trening 2"),
            createSession(race, race.thirdPractice, "Trening 3"),
            createSession(race, race.qualifying, "Kwalifikacje"),
            createSession(race, race.sprint, "Sprint"),
            createSession(race, race.sprintQualifying, "Sprint Kwal."),
            if (race.date != null && race.time != null) {
                parseSession(race.raceName, "Wyścig", race.date, race.time)
            } else null
        )
    }

    private fun createSession(
        race: Race,
        session: com.example.f1project.data.remote.Session?,
        name: String
    ): UpcomingSession? {
        return if (session?.date != null && session.time != null) {
            parseSession(race.raceName, name, session.date, session.time)
        } else null
    }

    private fun parseSession(
        raceName: String,
        sessionName: String,
        date: String,
        time: String
    ): UpcomingSession? {
        return try {
            val dateTime = ZonedDateTime.parse("${date}T${time}")
            UpcomingSession(raceName, sessionName, dateTime)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val now = ZonedDateTime.now()
                val state = _uiState.value
                _uiState.value = state.copy(
                    raceCountdown = calculateTimeLeft(state.nextRaceSession?.dateTime, now),
                    sessionCountdown = calculateTimeLeft(state.nextUpcomingSession?.dateTime, now)
                )
                delay(1000)
            }
        }
    }

    private fun calculateTimeLeft(target: ZonedDateTime?, now: ZonedDateTime): String {
        if (target == null) return "Brak danych"
        val duration = Duration.between(now, target)
        if (duration.isNegative) return "W trakcie..."
        val d = duration.toDays()
        val h = duration.toHours() % 24
        val m = duration.toMinutes() % 60
        val s = duration.seconds % 60
        return if (d > 0) "%02d D %02d H %02d M %02d S".format(d, h, m, s)
        else "%02d H %02d M %02d S".format(h, m, s)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}