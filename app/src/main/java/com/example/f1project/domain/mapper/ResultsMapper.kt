package com.example.f1project.domain.mapper

import com.example.f1project.data.remote.QualifyingResult
import com.example.f1project.data.remote.RaceResult
import com.example.f1project.domain.model.DomainResult

object ResultsMapper {

    fun mapRaceResult(dto: RaceResult): DomainResult {
        return DomainResult(
            position        = dto.position?.toIntOrNull() ?: 0,
            driverFullName  = "${dto.driver.givenName ?: ""} ${dto.driver.familyName ?: ""}".trim(),
            constructorName = dto.constructor.name ?: "",
            constructorId   = dto.constructor.constructorId ?: "",
            nationality     = dto.driver.nationality ?: "",
            points          = dto.points?.toDoubleOrNull(),
            timeOrStatus    = dto.time?.time ?: dto.status,
            q1 = null,
            q2 = null,
            q3 = null
        )
    }

    fun mapQualifyingResult(dto: QualifyingResult): DomainResult {
        return DomainResult(
            position        = dto.position?.toIntOrNull() ?: 0,
            driverFullName  = "${dto.driver.givenName ?: ""} ${dto.driver.familyName ?: ""}".trim(),
            constructorName = dto.constructor.name ?: "",
            constructorId   = dto.constructor.constructorId ?: "",
            nationality     = dto.driver.nationality ?: "",
            points          = null,
            timeOrStatus    = null,
            q1 = dto.q1,
            q2 = dto.q2,
            q3 = dto.q3
        )
    }

    fun mapRaceResultList(dtos: List<RaceResult>): List<DomainResult> =
        dtos.map { mapRaceResult(it) }

    fun mapQualifyingResultList(dtos: List<QualifyingResult>): List<DomainResult> =
        dtos.map { mapQualifyingResult(it) }
}