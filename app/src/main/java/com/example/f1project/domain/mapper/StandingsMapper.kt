package com.example.f1project.domain.mapper

import com.example.f1project.data.remote.ConstructorStanding
import com.example.f1project.data.remote.DriverStanding
import com.example.f1project.domain.model.DomainConstructor
import com.example.f1project.domain.model.DomainConstructorStanding
import com.example.f1project.domain.model.DomainDriver
import com.example.f1project.domain.model.DomainDriverStanding

object StandingsMapper {

    fun mapDriver(dto: DriverStanding): DomainDriverStanding {
        return DomainDriverStanding(
            // ZMIANA: ?.toIntOrNull() zamiast .toIntOrNull()
            position = dto.position?.toIntOrNull() ?: 0,
            points   = dto.points?.toDoubleOrNull() ?: 0.0,
            wins     = dto.wins?.toIntOrNull() ?: 0,
            driver = DomainDriver(
                driverId     = dto.driver.driverId ?: "",
                fullName     = "${dto.driver.givenName ?: ""} ${dto.driver.familyName ?: ""}".trim(),
                code         = dto.driver.code ?: "",
                number       = "",
                nationality  = dto.driver.nationality ?: "",
                dateOfBirth  = "",
                wikipediaUrl = ""
            ),
            constructor = dto.constructors.firstOrNull()?.let {
                DomainConstructor(
                    constructorId = it.constructorId ?: "",
                    name          = it.name ?: "",
                    nationality   = it.nationality ?: "",
                    wikipediaUrl  = ""
                )
            }
        )
    }

    fun mapDriverList(dtos: List<DriverStanding>): List<DomainDriverStanding> =
        dtos.map { mapDriver(it) }

    fun mapConstructor(dto: ConstructorStanding): DomainConstructorStanding {
        return DomainConstructorStanding(
            position = dto.position?.toIntOrNull() ?: 0,
            points   = dto.points?.toDoubleOrNull() ?: 0.0,
            wins     = dto.wins?.toIntOrNull() ?: 0,
            constructor = DomainConstructor(
                constructorId = dto.constructor.constructorId ?: "",
                name          = dto.constructor.name ?: "",
                nationality   = dto.constructor.nationality ?: "",
                wikipediaUrl  = ""
            )
        )
    }

    fun mapConstructorList(dtos: List<ConstructorStanding>): List<DomainConstructorStanding> =
        dtos.map { mapConstructor(it) }
}