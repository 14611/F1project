package com.example.f1project.domain.mapper

import com.example.f1project.data.remote.DriverDetail
import com.example.f1project.data.remote.DriverSeasonResult
import com.example.f1project.data.remote.ConstructorDetail
import com.example.f1project.data.remote.ConstructorSeasonResult
import com.example.f1project.domain.model.DomainConstructor
import com.example.f1project.domain.model.DomainConstructorProfile
import com.example.f1project.domain.model.DomainConstructorRaceResult
import com.example.f1project.domain.model.DomainDriver
import com.example.f1project.domain.model.DomainDriverProfile
import com.example.f1project.domain.model.DomainDriverResult
import com.example.f1project.domain.model.DomainRaceResult
import com.example.f1project.domain.model.DomainSeasonStats

object ProfileMapper {

    fun mapDriverProfile(
        detail: DriverDetail,
        currentTeam: String,
        seasonResults: List<DriverSeasonResult>
    ): DomainDriverProfile {

        val raceResults = seasonResults.mapNotNull { race ->
            val result = race.results?.firstOrNull() ?: return@mapNotNull null
            DomainRaceResult(
                round = race.round.toIntOrNull() ?: 0,
                raceName = race.raceName,
                position = result.position.toIntOrNull() ?: 0,
                points = result.points.toDoubleOrNull() ?: 0.0,
                gridPosition = result.grid.toIntOrNull() ?: 0,
                status = result.status
            )
        }

        val stats = DomainSeasonStats(
            points = raceResults.sumOf { it.points.toInt() },
            wins = raceResults.count { it.position == 1 },
            podiums = raceResults.count { it.position <= 3 }
        )

        return DomainDriverProfile(
            driver = DomainDriver(
                driverId = detail.driverId,
                fullName = "${detail.givenName} ${detail.familyName}",
                code = detail.code ?: "",
                number = detail.permanentNumber ?: "",
                nationality = detail.nationality,
                dateOfBirth = detail.dateOfBirth ?: "",
                wikipediaUrl = detail.url ?: ""
            ),
            currentTeam = currentTeam,
            seasonStats = stats,
            raceResults = raceResults
        )
    }

    fun mapConstructorProfile(
        detail: ConstructorDetail,
        seasonResults: List<ConstructorSeasonResult>
    ): DomainConstructorProfile {

        val raceResults = seasonResults.mapNotNull { race ->
            val results = race.results ?: return@mapNotNull null
            if (results.isEmpty()) return@mapNotNull null

            val driver1 = results.getOrNull(0)
            val driver2 = results.getOrNull(1)

            DomainConstructorRaceResult(
                round = race.round.toIntOrNull() ?: 0,
                raceName = race.raceName,
                driver1 = DomainDriverResult(
                    fullName = driver1?.let {
                        "${it.driver.givenName} ${it.driver.familyName}"
                    } ?: "",
                    position = driver1?.position?.toIntOrNull() ?: 0,
                    points = driver1?.points?.toDoubleOrNull() ?: 0.0
                ),
                driver2 = driver2?.let {
                    DomainDriverResult(
                        fullName = "${it.driver.givenName} ${it.driver.familyName}",
                        position = it.position.toIntOrNull() ?: 0,
                        points = it.points.toDoubleOrNull() ?: 0.0
                    )
                }
            )
        }

        val allResults = seasonResults.flatMap { it.results ?: emptyList() }
        val stats = DomainSeasonStats(
            points = allResults.sumOf { it.points.toDoubleOrNull()?.toInt() ?: 0 },
            wins = allResults.count { it.position == "1" },
            podiums = allResults.count {
                it.position.toIntOrNull()?.let { p -> p <= 3 } ?: false
            }
        )

        return DomainConstructorProfile(
            constructor = DomainConstructor(
                constructorId = detail.constructorId,
                name = detail.name,
                nationality = detail.nationality,
                wikipediaUrl = detail.url ?: ""
            ),
            seasonStats = stats,
            raceResults = raceResults
        )
    }
}