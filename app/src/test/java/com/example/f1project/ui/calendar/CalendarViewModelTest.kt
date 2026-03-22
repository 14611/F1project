package com.example.f1project.ui.calendar

import com.example.f1project.data.F1Repository
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.ApiRaceResponse
import com.example.f1project.data.remote.Circuit
import com.example.f1project.data.remote.Location
import com.example.f1project.data.remote.MRDataRaces
import com.example.f1project.data.remote.Race
import com.example.f1project.data.remote.RaceTable
import com.example.f1project.data.remote.Session
import com.example.f1project.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: F1Repository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        repository = mockk()
        viewModel = CalendarViewModel(repository)
    }

    @Test
    fun `loadSeason zwraca liste wyscigow`() = runTest {
        coEvery { repository.getRaceSchedule("2025") } returns
                RepositoryResult.Fresh(buildRaceResponse())

        viewModel.loadSeason("2025")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.races.size)
        assertEquals("Bahrain Grand Prix", state.races[0].name)
        assertEquals("Bahrain International Circuit", state.races[0].circuitName)
        assertEquals("Bahrain", state.races[0].country)
    }

    @Test
    fun `loadSeason ustawia error gdy brak danych i polaczenia`() = runTest {
        coEvery { repository.getRaceSchedule("2025") } returns
                RepositoryResult.Error("Timeout")

        viewModel.loadSeason("2025")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.races.isEmpty())
        assertEquals("Błąd: Timeout", state.error)
    }

    @Test
    fun `loadSeason mapuje sesje wyscigu poprawnie`() = runTest {
        coEvery { repository.getRaceSchedule("2025") } returns
                RepositoryResult.Fresh(buildRaceResponse())

        viewModel.loadSeason("2025")

        val race = viewModel.uiState.value.races.first()
        assertTrue(race.sessions.any { it.name == "Trening 1" })
        assertTrue(race.sessions.any { it.name == "Wyścig" && it.isRace })
    }

    @Test
    fun `loadSeason ustawia isFromCache gdy dane z cache`() = runTest {
        coEvery { repository.getRaceSchedule("2025") } returns
                RepositoryResult.Cached(buildRaceResponse())

        viewModel.loadSeason("2025")

        assertTrue(viewModel.uiState.value.isFromCache)
    }

    @Test
    fun `loadSeason dla roznych sezonow zwraca rozne dane`() = runTest {
        coEvery { repository.getRaceSchedule("2024") } returns
                RepositoryResult.Fresh(buildRaceResponse(raceName = "Abu Dhabi Grand Prix 2024"))
        coEvery { repository.getRaceSchedule("2025") } returns
                RepositoryResult.Fresh(buildRaceResponse(raceName = "Bahrain Grand Prix 2025"))

        viewModel.loadSeason("2024")
        assertEquals("Abu Dhabi Grand Prix 2024", viewModel.uiState.value.races[0].name)

        viewModel.loadSeason("2025")
        assertEquals("Bahrain Grand Prix 2025", viewModel.uiState.value.races[0].name)
    }

    @Test
    fun `loadSeason poprawnie mapuje numer rundy na Int`() = runTest {
        coEvery { repository.getRaceSchedule("2025") } returns
                RepositoryResult.Fresh(buildRaceResponse(round = "5"))

        viewModel.loadSeason("2025")

        assertEquals(5, viewModel.uiState.value.races[0].round)
    }

    private fun buildRaceResponse(
        raceName: String = "Bahrain Grand Prix",
        round: String = "1"
    ): ApiRaceResponse {
        return ApiRaceResponse(
            mrData = MRDataRaces(
                raceTable = RaceTable(
                    season = "2025",
                    races = listOf(
                        Race(
                            season = "2025",
                            round = round,
                            raceName = raceName,
                            circuit = Circuit(
                                circuitId = "bahrain",
                                circuitName = "Bahrain International Circuit",
                                location = Location(
                                    country = "Bahrain",
                                    locality = "Sakhir"
                                )
                            ),
                            date = "2025-03-02",
                            time = "15:00:00Z",
                            firstPractice = Session(
                                date = "2025-02-28",
                                time = "11:30:00Z"
                            ),
                            secondPractice = null,
                            thirdPractice = null,
                            qualifying = Session(
                                date = "2025-03-01",
                                time = "15:00:00Z"
                            ),
                            sprint = null,
                            sprintQualifying = null
                        )
                    )
                )
            )
        )
    }
}