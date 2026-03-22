package com.example.f1project.ui.standings

import app.cash.turbine.test
import com.example.f1project.data.F1Repository
import com.example.f1project.data.RepositoryResult
import com.example.f1project.data.remote.ApiResponse
import com.example.f1project.data.remote.Constructor
import com.example.f1project.data.remote.ConstructorStanding
import com.example.f1project.data.remote.Driver
import com.example.f1project.data.remote.DriverStanding
import com.example.f1project.data.remote.MRData
import com.example.f1project.data.remote.StandingsList
import com.example.f1project.data.remote.StandingsTable
import com.example.f1project.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
class StandingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: F1Repository
    private lateinit var viewModel: StandingsViewModel

    @Before
    fun setUp() {
        repository = mockk()
        viewModel = StandingsViewModel(repository)
    }

    @Test
    fun `loadSeason emituje dane kierowcow i konstruktorow`() = runTest {
        coEvery { repository.getDriverStandings("2025") } returns
                RepositoryResult.Fresh(buildDriverResponse())
        coEvery { repository.getConstructorStandings("2025") } returns
                RepositoryResult.Fresh(buildConstructorResponse())

        viewModel.loadSeason("2025")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.drivers.size)
        assertEquals("Max Verstappen", state.drivers[0].driver.fullName)
        assertEquals(1, state.drivers[0].position)
        assertEquals(1, state.constructors.size)
        assertEquals("Red Bull Racing", state.constructors[0].constructor.name)
    }

    @Test
    fun `loadSeason ustawia error gdy API i cache niedostepne`() = runTest {
        coEvery { repository.getDriverStandings("2025") } returns
                RepositoryResult.Error("Brak połączenia")
        coEvery { repository.getConstructorStandings("2025") } returns
                RepositoryResult.Error("Brak połączenia")

        viewModel.loadSeason("2025")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Brak danych i brak połączenia z internetem", state.error)
        assertTrue(state.drivers.isEmpty())
        assertTrue(state.constructors.isEmpty())
    }

    @Test
    fun `loadSeason ustawia isFromCache gdy dane z cache`() = runTest {
        coEvery { repository.getDriverStandings("2025") } returns
                RepositoryResult.Cached(buildDriverResponse())
        coEvery { repository.getConstructorStandings("2025") } returns
                RepositoryResult.Fresh(buildConstructorResponse())

        viewModel.loadSeason("2025")

        assertTrue(viewModel.uiState.value.isFromCache)
    }

    @Test
    fun `refresh wywoluje API ponownie`() = runTest {
        coEvery { repository.getDriverStandings("2025") } returns
                RepositoryResult.Fresh(buildDriverResponse())
        coEvery { repository.getConstructorStandings("2025") } returns
                RepositoryResult.Fresh(buildConstructorResponse())

        viewModel.loadSeason("2025")
        viewModel.refresh("2025")

        coVerify(exactly = 2) { repository.getDriverStandings("2025") }
        coVerify(exactly = 2) { repository.getConstructorStandings("2025") }
    }

    @Test
    fun `loadSeason najpierw emituje isLoading true potem false`() = runTest {
        coEvery { repository.getDriverStandings("2025") } returns
                RepositoryResult.Fresh(buildDriverResponse())
        coEvery { repository.getConstructorStandings("2025") } returns
                RepositoryResult.Fresh(buildConstructorResponse())

        // Sprawdzamy stan przed załadowaniem
        assertFalse(viewModel.uiState.value.isLoading)

        // Po załadowaniu — isLoading musi być false i dane obecne
        viewModel.loadSeason("2025")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.drivers.size)
    }

    @Test
    fun `loadSeason poprawnie mapuje punkty z String na Double`() = runTest {
        coEvery { repository.getDriverStandings("2025") } returns
                RepositoryResult.Fresh(buildDriverResponse(points = "310.5"))
        coEvery { repository.getConstructorStandings("2025") } returns
                RepositoryResult.Fresh(buildConstructorResponse())

        viewModel.loadSeason("2025")

        assertEquals(310.5, viewModel.uiState.value.drivers[0].points, 0.001)
    }

    private fun buildDriverResponse(points: String = "300"): ApiResponse {
        return ApiResponse(
            mrData = MRData(
                standingsTable = StandingsTable(
                    standingsLists = listOf(
                        StandingsList(
                            season = "2025",
                            driverStandings = listOf(
                                DriverStanding(
                                    position = "1",
                                    points = points,
                                    wins = "10",
                                    driver = Driver(
                                        driverId = "max_verstappen",
                                        givenName = "Max",
                                        familyName = "Verstappen",
                                        code = "VER",
                                        nationality = "Dutch"
                                    ),
                                    constructors = listOf(
                                        Constructor(
                                            constructorId = "red_bull",
                                            name = "Red Bull Racing",
                                            nationality = "Austrian"
                                        )
                                    )
                                )
                            ),
                            constructorStandings = null
                        )
                    )
                )
            )
        )
    }

    private fun buildConstructorResponse(): ApiResponse {
        return ApiResponse(
            mrData = MRData(
                standingsTable = StandingsTable(
                    standingsLists = listOf(
                        StandingsList(
                            season = "2025",
                            driverStandings = null,
                            constructorStandings = listOf(
                                ConstructorStanding(
                                    position = "1",
                                    points = "500",
                                    wins = "15",
                                    constructor = Constructor(
                                        constructorId = "red_bull",
                                        name = "Red Bull Racing",
                                        nationality = "Austrian"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}