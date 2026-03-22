package com.example.f1project.domain.mapper

import com.example.f1project.data.remote.Race
import com.example.f1project.domain.model.DomainRace
import com.example.f1project.domain.model.DomainSession

object RaceMapper {

    fun map(dto: Race): DomainRace {
        val sessions = buildList {
            dto.firstPractice?.let {
                add(DomainSession("Trening 1", it.date, it.time))
            }
            dto.secondPractice?.let {
                add(DomainSession("Trening 2", it.date, it.time))
            }
            dto.thirdPractice?.let {
                add(DomainSession("Trening 3", it.date, it.time))
            }
            dto.sprintQualifying?.let {
                add(DomainSession("Kwal. do Sprintu", it.date, it.time))
            }
            dto.sprint?.let {
                add(DomainSession("Sprint", it.date, it.time))
            }
            dto.qualifying?.let {
                add(DomainSession("Kwalifikacje (GP)", it.date, it.time))
            }
            if (dto.date != null && dto.time != null) {
                add(DomainSession("Wyścig", dto.date, dto.time, isRace = true))
            }
        }

        return DomainRace(
            season = dto.season,
            round = dto.round.toIntOrNull() ?: 0,
            name = dto.raceName,
            circuitName = dto.circuit.circuitName,
            country = dto.circuit.location.country,
            locality = dto.circuit.location.locality,
            raceDate = dto.date,
            raceTime = dto.time,
            sessions = sessions
        )
    }

    fun mapList(dtos: List<Race>): List<DomainRace> = dtos.map { map(it) }
}