package com.example.f1project.data

import android.util.Log
import com.example.f1project.data.remote.OpenF1Lap
import com.example.f1project.data.remote.OpenF1RetrofitInstance
import com.example.f1project.domain.model.DomainResult
import java.time.Instant

class OpenF1Repository {

    suspend fun getSprintQualifyingResults(
        year: Int,
        round: Int,
        location: String
    ): OpenF1Result<List<DomainResult>> {
        return try {
            val allSessions = OpenF1RetrofitInstance.api.getAllSessions(year = year)

            val sprintQualifyingSessions = allSessions.filter { session ->
                val name = session.sessionName.lowercase()
                name.contains("sprint qualifying") || name.contains("sprint shootout")
            }.sortedBy { it.sessionKey }

            if (sprintQualifyingSessions.isEmpty()) {
                return OpenF1Result.Error("Brak sesji Sprint Qualifying dla sezonu $year")
            }

            val session = sprintQualifyingSessions.firstOrNull { s ->
                val openF1Location = s.location?.lowercase()?.trim() ?: ""
                val jolpicaLocation = location.lowercase().trim()
                openF1Location.contains(jolpicaLocation) ||
                        jolpicaLocation.contains(openF1Location) ||
                        matchSpecialLocations(jolpicaLocation, openF1Location)
            } ?: sprintQualifyingSessions.getOrNull(round - 1)
            ?: sprintQualifyingSessions.last()

            Log.d("OpenF1", "Sesja: ${session.sessionKey} @ ${session.location}")

            val laps = OpenF1RetrofitInstance.api.getLaps(session.sessionKey)
            val drivers = OpenF1RetrofitInstance.api.getDrivers(session.sessionKey)

            drivers.take(3).forEach { driver ->
                Log.d("OpenF1Flag", "Kierowca: ${driver.fullName}, countryCode: '${driver.countryCode}'")
            }

            if (laps.isEmpty()) {
                return OpenF1Result.Error(
                    "Brak danych dla ${session.sessionName} @ ${session.location}.\n" +
                            "Sesja mogła jeszcze nie odbyć się."
                )
            }

            val driversMap = drivers.associateBy { it.driverNumber }

            val validLaps = laps.filter { lap ->
                lap.lapDuration != null &&
                        lap.lapDuration > 0 &&
                        lap.isPitOutLap != true &&
                        lap.dateStart != null
            }

            val segments = splitIntoSegments(validLaps)
            Log.d("OpenF1", "Segmenty: SQ1=${segments[0].size}, SQ2=${segments[1].size}, SQ3=${segments[2].size} okrążeń")

            val sq1BestPerDriver = bestLapPerDriver(segments[0])
            val sq2BestPerDriver = bestLapPerDriver(segments[1])
            val sq3BestPerDriver = bestLapPerDriver(segments[2])

            val allDriverNumbers = (
                    sq1BestPerDriver.keys +
                            sq2BestPerDriver.keys +
                            sq3BestPerDriver.keys
                    ).toSet()

            val results: List<DomainResult> = allDriverNumbers
                .map { driverNumber ->
                    val sq1Time = sq1BestPerDriver[driverNumber]?.lapDuration
                    val sq2Time = sq2BestPerDriver[driverNumber]?.lapDuration
                    val sq3Time = sq3BestPerDriver[driverNumber]?.lapDuration
                    val bestTime = listOfNotNull(sq1Time, sq2Time, sq3Time).minOrNull()
                    Triple(driverNumber, bestTime, Triple(sq1Time, sq2Time, sq3Time))
                }
                .sortedWith(compareBy(nullsLast()) { it.second })
                .mapIndexed { index, (driverNumber, _, times) ->
                    val driver = driversMap[driverNumber]
                    val (sq1, sq2, sq3) = times
                    // ZMIANA: DomainResult zamiast DisplayResult
                    DomainResult(
                        position        = index + 1,
                        driverFullName  = driver?.fullName ?: "Kierowca #$driverNumber",
                        constructorName = driver?.teamName ?: "Nieznany zespół",
                        constructorId   = mapTeamNameToId(driver?.teamName),
                        nationality     = driver?.countryCode ?: "",
                        points          = null,
                        timeOrStatus    = null,
                        q1              = sq1?.let { formatLapTime(it) },
                        q2              = sq2?.let { formatLapTime(it) },
                        q3              = sq3?.let { formatLapTime(it) }
                    )
                }

            OpenF1Result.Success(results)
        } catch (e: Exception) {
            Log.e("OpenF1", "Błąd: ${e.message}", e)
            OpenF1Result.Error("Błąd OpenF1: ${e.message}")
        }
    }

    private fun splitIntoSegments(laps: List<OpenF1Lap>): List<List<OpenF1Lap>> {
        if (laps.isEmpty()) return listOf(emptyList(), emptyList(), emptyList())

        val sortedLaps = laps.sortedBy { parseInstant(it.dateStart) }

        if (sortedLaps.size < 3) {
            return listOf(sortedLaps, emptyList(), emptyList())
        }

        val gaps = mutableListOf<Pair<Int, Long>>()
        for (i in 0 until sortedLaps.size - 1) {
            val currentTime = parseInstant(sortedLaps[i].dateStart) ?: continue
            val nextTime = parseInstant(sortedLaps[i + 1].dateStart) ?: continue
            val gap = nextTime - currentTime
            if (gap > 0) gaps.add(Pair(i, gap))
        }

        val sortedGaps = gaps.sortedByDescending { it.second }

        if (sortedGaps.size < 2) {
            return listOf(sortedLaps, emptyList(), emptyList())
        }

        val boundaries = sortedGaps.take(2).map { it.first }.sorted()
        val boundary1 = boundaries[0] + 1
        val boundary2 = boundaries[1] + 1

        Log.d("OpenF1", "Granice: SQ1=0..$boundary1, SQ2=$boundary1..$boundary2, SQ3=$boundary2..${sortedLaps.size}")

        return listOf(
            sortedLaps.subList(0, boundary1),
            sortedLaps.subList(boundary1, boundary2),
            sortedLaps.subList(boundary2, sortedLaps.size)
        )
    }

    private fun bestLapPerDriver(laps: List<OpenF1Lap>): Map<Int, OpenF1Lap> {
        return laps
            .groupBy { it.driverNumber }
            .mapValues { (_, driverLaps) ->
                driverLaps.minByOrNull { it.lapDuration!! }!!
            }
    }

    private fun parseInstant(dateString: String?): Long? {
        if (dateString == null) return null
        return try {
            Instant.parse(dateString).epochSecond
        } catch (e: Exception) {
            null
        }
    }

    private fun matchSpecialLocations(jolpica: String, openF1: String): Boolean {
        val specialMappings = mapOf(
            "monte-carlo"   to "monaco",
            "sao paulo"     to "são paulo",
            "marina bay"    to "singapore",
            "americas"      to "austin",
            "yas island"    to "abu dhabi",
            "miami gardens" to "miami"
        )
        return specialMappings[jolpica] == openF1 ||
                specialMappings[openF1] == jolpica
    }

    private fun formatLapTime(seconds: Double?): String {
        if (seconds == null) return "Brak czasu"
        val minutes = (seconds / 60).toInt()
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "%d:%06.3f".format(minutes, remainingSeconds)
        } else {
            "%.3f".format(remainingSeconds)
        }
    }

    private fun mapTeamNameToId(teamName: String?): String {
        return when (teamName?.lowercase()) {
            "red bull racing"       -> "red_bull"
            "ferrari"               -> "ferrari"
            "mercedes"              -> "mercedes"
            "mclaren"               -> "mclaren"
            "aston martin"          -> "aston_martin"
            "alpine"                -> "alpine"
            "williams"              -> "williams"
            "rb", "racing bulls"    -> "rb"
            "kick sauber", "sauber" -> "sauber"
            "haas f1 team", "haas"  -> "haas"
            else                    -> teamName?.lowercase() ?: ""
        }
    }
}

sealed class OpenF1Result<out T> {
    data class Success<T>(val data: T) : OpenF1Result<T>()
    data class Error(val message: String) : OpenF1Result<Nothing>()
}