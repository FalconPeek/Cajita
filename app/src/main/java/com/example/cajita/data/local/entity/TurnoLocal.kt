package com.example.cajita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class TurnoLocal(
    @PrimaryKey
    val id: String,
    val cajero: String?,
    val abiertoEn: Long,
    val montoAperturaDeclarado: Long,
    val estado: String, // "ABIERTO" | "CERRADO"
    val sincronizadoApertura: Boolean = false,
    val sincronizadoCierre: Boolean = false
)
