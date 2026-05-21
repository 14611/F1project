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

            val laps    = OpenF1RetrofitInstance.api.getLaps(session.sessionKey)
            val drivers = OpenF1RetrofitInstance.api.getDrivers(session.sessionKey)

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
            Log.d("OpenF1",
                "Segmenty: SQ1=${segments[0].size}, " +
                        "SQ2=${segments[1].size}, " +
                        "SQ3=${segments[2].size} okrążeń"
            )

            val sq1Best = bestLapPerDriver(segments[0])
            val sq2Best = bestLapPerDriver(segments[1])
            val sq3Best = bestLapPerDriver(segments[2])

            val allDriverNumbers = (sq1Best.keys + sq2Best.keys + sq3Best.keys).toSet()

            val results: List<DomainResult> = allDriverNumbers
                .map { driverNumber ->
                    val sq1Time = sq1Best[driverNumber]?.lapDuration
                    val sq2Time = sq2Best[driverNumber]?.lapDuration
                    val sq3Time = sq3Best[driverNumber]?.lapDuration
                    val bestTime = listOfNotNull(sq1Time, sq2Time, sq3Time).minOrNull()
                    Triple(driverNumber, bestTime, Triple(sq1Time, sq2Time, sq3Time))
                }
                .sortedWith(compareBy(nullsLast()) { it.second })
                .mapIndexed { index, (driverNumber, _, times) ->
                    val driver = driversMap[driverNumber]
                    val (sq1, sq2, sq3) = times
                    DomainResult(
                        position       = index + 1,
                        driverFullName = driver?.fullName ?: "Kierowca #$driverNumber",
                        constructorName = driver?.teamName ?: "Nieznany zespół",
                        constructorId   = mapTeamNameToId(driver?.teamName),
                        nationality     = driver?.countryCode ?: "",
                        points          = null,
                        timeOrStatus    = null,
                        q1 = sq1?.let { formatLapTime(it) },
                        q2 = sq2?.let { formatLapTime(it) },
                        q3 = sq3?.let { formatLapTime(it) }
                    )
                }

            OpenF1Result.Success(results)

        } catch (e: Exception) {
            Log.e("OpenF1", "Błąd: ${e.message}", e)
            OpenF1Result.Error("Błąd OpenF1: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Podział okrążeń na segmenty SQ1 / SQ2 / SQ3
    //
    // STRATEGIA GŁÓWNA — analiza zmian zbioru kierowców:
    //
    // Czerwona flaga w SQ1:          ci sami kierowcy wracają → brak redukcji → NIE boundary
    // Koniec SQ1, start SQ2:         wyeliminowani nie wracają → redukcja → TAK boundary
    //
    // Algorytm:
    //   1. Znajdź wszystkie przerwy >= 2 minuty między okrążeniami
    //   2. Dla każdej przerwy porównaj kierowców przed i po (okno 30 okrążeń)
    //   3. Przerwa = granica segmentu jeśli: wiele kierowców NIE wraca i mało nowych przybywa
    //   4. Weź 2 najwcześniejsze chronologicznie granice
    //
    // FALLBACK — statystyczny (gdy analiza kierowców zawiedzie):
    //   Weź 2 największe przerwy czasowe (oryginalna logika)
    // ─────────────────────────────────────────────────────────────────────────
    private fun splitIntoSegments(laps: List<OpenF1Lap>): List<List<OpenF1Lap>> {
        if (laps.isEmpty()) return listOf(emptyList(), emptyList(), emptyList())

        val sorted = laps.sortedBy { parseInstant(it.dateStart) }
        if (sorted.size < 6) return listOf(sorted, emptyList(), emptyList())

        // Parametry
        val MIN_PAUSE_SECONDS = 120L  // przerwa >= 2 min to kandydat na granicę
        val WINDOW_SIZE       = 30    // okrążenia do analizy przed/po przerwie
        // Kierowcy: SQ1≈20, SQ2≈15, SQ3≈8 — eliminacja >=2 to pewny sygnał
        val MIN_ELIMINATED    = 2
        // Fuzja: nie więcej niż 3 "nowych" kierowców po granicy (fluktuacje danych)
        val MAX_NEW_DRIVERS   = 3

        data class PauseInfo(
            val index: Int,           // indeks ostatniego okrążenia PRZED przerwą
            val gapSeconds: Long,
            val eliminated: Int,      // kierowcy którzy zniknęli po przerwie
            val newDrivers: Int,      // kierowcy którzy pojawili się po przerwie
            val totalBefore: Int,     // łączna liczba kierowców przed przerwą
            val isSegmentBoundary: Boolean
        )

        val pauses = mutableListOf<PauseInfo>()

        for (i in 0 until sorted.size - 1) {
            val t1  = parseInstant(sorted[i].dateStart)     ?: continue
            val t2  = parseInstant(sorted[i + 1].dateStart) ?: continue
            val gap = t2 - t1

            if (gap < MIN_PAUSE_SECONDS) continue

            // Okno kierowców PRZED przerwą
            val beforeSlice = sorted.subList(maxOf(0, i - WINDOW_SIZE + 1), i + 1)
            val driversBefore = beforeSlice.map { it.driverNumber }.toSet()

            // Okno kierowców PO przerwie
            val afterSlice = sorted.subList(
                i + 1, minOf(sorted.size, i + 1 + WINDOW_SIZE)
            )
            val driversAfter = afterSlice.map { it.driverNumber }.toSet()

            val eliminatedCount = (driversBefore - driversAfter).size
            val newCount        = (driversAfter - driversBefore).size

            // Granica segmentu:
            //   • co najmniej MIN_ELIMINATED kierowców nie wraca (wyeliminowani)
            //   • nie więcej niż MAX_NEW_DRIVERS nowych (bez sensu w kwalifikacjach)
            //   • musimy mieć wystarczająco dużo kierowców przed (filtr szumów)
            val isBoundary = eliminatedCount >= MIN_ELIMINATED &&
                    newCount <= MAX_NEW_DRIVERS &&
                    driversBefore.size >= 5

            pauses.add(
                PauseInfo(i, gap, eliminatedCount, newCount, driversBefore.size, isBoundary)
            )

            Log.d("OpenF1Seg",
                "Przerwa i=$i gap=${gap}s | " +
                        "przed=${driversBefore.size} po=${driversAfter.size} | " +
                        "wyelim=$eliminatedCount nowi=$newCount | " +
                        "granica=$isBoundary"
            )
        }

        // ── Strategia główna: analiza kierowców ──────────────────────────────
        val confirmedBoundaries = pauses
            .filter { it.isSegmentBoundary }
            .sortedBy  { it.index }  // chronologicznie = SQ1→SQ2, potem SQ2→SQ3
            .take(2)

        if (confirmedBoundaries.size >= 2) {
            val b1 = confirmedBoundaries[0].index
            val b2 = confirmedBoundaries[1].index
            Log.d("OpenF1Seg", "Strategia GŁÓWNA: granice na $b1, $b2 (analiza kierowców)")
            return listOf(
                sorted.subList(0,      b1 + 1),
                sorted.subList(b1 + 1, b2 + 1),
                sorted.subList(b2 + 1, sorted.size)
            )
        }

        // Jedna potwierdzona granica — szukaj drugiej po czasie
        if (confirmedBoundaries.size == 1) {
            val confirmed = confirmedBoundaries[0]
            // Spośród pozostałych przerw (niepotwierdzonych), weź największą
            val secondCandidate = pauses
                .filter { it.index != confirmed.index }
                .maxByOrNull { it.gapSeconds }

            if (secondCandidate != null) {
                val boundaries = listOf(confirmed.index, secondCandidate.index).sorted()
                val b1 = boundaries[0]; val b2 = boundaries[1]
                Log.d("OpenF1Seg",
                    "Strategia MIESZANA: granice na $b1 (pewna) + $b2 (czas)"
                )
                return listOf(
                    sorted.subList(0,      b1 + 1),
                    sorted.subList(b1 + 1, b2 + 1),
                    sorted.subList(b2 + 1, sorted.size)
                )
            }
        }

        // ── Fallback: 2 największe przerwy czasowe (oryginalna logika) ────────
        Log.w("OpenF1Seg",
            "Fallback: analiza kierowców niewystarczająca, używam 2 największych przerw"
        )
        val topGaps = pauses.sortedByDescending { it.gapSeconds }.take(2)
        if (topGaps.size < 2) return listOf(sorted, emptyList(), emptyList())

        val fallback = topGaps.map { it.index }.sorted()
        val f1 = fallback[0]; val f2 = fallback[1]
        return listOf(
            sorted.subList(0,      f1 + 1),
            sorted.subList(f1 + 1, f2 + 1),
            sorted.subList(f2 + 1, sorted.size)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun bestLapPerDriver(laps: List<OpenF1Lap>): Map<Int, OpenF1Lap> =
        laps.groupBy { it.driverNumber }
            .mapValues { (_, driverLaps) -> driverLaps.minByOrNull { it.lapDuration!! }!! }

    private fun parseInstant(dateString: String?): Long? {
        if (dateString == null) return null
        return try {
            Instant.parse(dateString).epochSecond
        } catch (_: Exception) { null }
    }

    private fun matchSpecialLocations(jolpica: String, openF1: String): Boolean {
        val map = mapOf(
            "monte-carlo"   to "monaco",
            "sao paulo"     to "são paulo",
            "marina bay"    to "singapore",
            "americas"      to "austin",
            "yas island"    to "abu dhabi",
            "miami gardens" to "miami"
        )
        return map[jolpica] == openF1 || map[openF1] == jolpica
    }

    private fun formatLapTime(seconds: Double?): String {
        if (seconds == null) return "Brak czasu"
        val minutes          = (seconds / 60).toInt()
        val remainingSeconds = seconds % 60
        return if (minutes > 0) "%d:%06.3f".format(minutes, remainingSeconds)
        else "%.3f".format(remainingSeconds)
    }

    private fun mapTeamNameToId(teamName: String?): String =
        when (teamName?.lowercase()) {
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

sealed class OpenF1Result<out T> {
    data class Success<T>(val data: T)            : OpenF1Result<T>()
    data class Error(val message: String)          : OpenF1Result<Nothing>()
}