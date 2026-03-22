package com.example.f1project.domain.mapper

import com.example.f1project.data.remote.Constructor
import com.example.f1project.data.remote.ConstructorStanding
import com.example.f1project.data.remote.Driver
import com.example.f1project.data.remote.DriverStanding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StandingsMapperTest {

    @Test
    fun `mapDriver laczy imie i nazwisko w fullName`() {
        val dto = buildDriverStanding(givenName = "Max", familyName = "Verstappen")
        val result = StandingsMapper.mapDriver(dto)
        assertEquals("Max Verstappen", result.driver.fullName)
    }

    @Test
    fun `mapDriver konwertuje pozycje ze String na Int`() {
        val dto = buildDriverStanding(position = "3")
        val result = StandingsMapper.mapDriver(dto)
        assertEquals(3, result.position)
    }

    @Test
    fun `mapDriver konwertuje punkty ze String na Double`() {
        val dto = buildDriverStanding(points = "247.5")
        val result = StandingsMapper.mapDriver(dto)
        assertEquals(247.5, result.points, 0.001)
    }

    @Test
    fun `mapDriver zwraca 0 dla nieprawidlowej pozycji`() {
        val dto = buildDriverStanding(position = "abc")
        val result = StandingsMapper.mapDriver(dto)
        assertEquals(0, result.position)
    }

    @Test
    fun `mapDriver zachowuje driverId`() {
        val dto = buildDriverStanding(driverId = "max_verstappen")
        val result = StandingsMapper.mapDriver(dto)
        assertEquals("max_verstappen", result.driver.driverId)
    }

    @Test
    fun `mapDriver zachowuje nationality`() {
        val dto = buildDriverStanding(nationality = "Dutch")
        val result = StandingsMapper.mapDriver(dto)
        assertEquals("Dutch", result.driver.nationality)
    }

    @Test
    fun `mapDriver ustawia konstruktor null gdy lista pusta`() {
        val dto = buildDriverStanding(constructors = emptyList())
        val result = StandingsMapper.mapDriver(dto)
        assertNull(result.constructor)
    }

    @Test
    fun `mapDriver pobiera pierwszego konstruktora z listy`() {
        val dto = buildDriverStanding(
            constructors = listOf(
                Constructor("red_bull", "Red Bull Racing", "Austrian"),
                Constructor("mercedes", "Mercedes", "German")
            )
        )
        val result = StandingsMapper.mapDriver(dto)
        assertEquals("red_bull", result.constructor?.constructorId)
    }

    @Test
    fun `mapDriverList mapuje cala liste`() {
        val dtos = listOf(
            buildDriverStanding(position = "1", givenName = "Max", familyName = "Verstappen"),
            buildDriverStanding(position = "2", givenName = "Lando", familyName = "Norris")
        )
        val results = StandingsMapper.mapDriverList(dtos)
        assertEquals(2, results.size)
        assertEquals("Max Verstappen", results[0].driver.fullName)
        assertEquals("Lando Norris", results[1].driver.fullName)
    }

    @Test
    fun `mapDriverList zwraca pusta liste dla pustego wejscia`() {
        val results = StandingsMapper.mapDriverList(emptyList())
        assertEquals(0, results.size)
    }

    @Test
    fun `mapConstructor zachowuje constructorId`() {
        val dto = ConstructorStanding(
            position = "1",
            points = "500",
            wins = "10",
            constructor = Constructor(
                constructorId = "red_bull",
                name = "Red Bull Racing",
                nationality = "Austrian"
            )
        )
        val result = StandingsMapper.mapConstructor(dto)
        assertEquals("red_bull", result.constructor.constructorId)
        assertEquals("Red Bull Racing", result.constructor.name)
    }

    @Test
    fun `mapConstructor konwertuje punkty na Double`() {
        val dto = ConstructorStanding(
            position = "1",
            points = "600.5",
            wins = "12",
            constructor = Constructor("ferrari", "Ferrari", "Italian")
        )
        val result = StandingsMapper.mapConstructor(dto)
        assertEquals(600.5, result.points, 0.001)
    }

    @Test
    fun `mapConstructorList mapuje cala liste`() {
        val dtos = listOf(
            ConstructorStanding("1", "500", "10",
                Constructor("red_bull", "Red Bull Racing", "Austrian")),
            ConstructorStanding("2", "400", "5",
                Constructor("ferrari", "Ferrari", "Italian"))
        )
        val results = StandingsMapper.mapConstructorList(dtos)
        assertEquals(2, results.size)
        assertEquals("Red Bull Racing", results[0].constructor.name)
        assertEquals("Ferrari", results[1].constructor.name)
    }

    private fun buildDriverStanding(
        position: String = "1",
        points: String = "300",
        wins: String = "10",
        driverId: String = "max_verstappen",
        givenName: String = "Max",
        familyName: String = "Verstappen",
        nationality: String = "Dutch",
        constructors: List<Constructor> = listOf(
            Constructor("red_bull", "Red Bull Racing", "Austrian")
        )
    ) = DriverStanding(
        position = position,
        points = points,
        wins = wins,
        driver = Driver(
            driverId = driverId,
            givenName = givenName,
            familyName = familyName,
            code = "VER",
            nationality = nationality
        ),
        constructors = constructors
    )
}