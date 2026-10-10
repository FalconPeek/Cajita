package com.example.cajita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cierres")
data class CierreLocal(
    @PrimaryKey
    val id: String,
    val turnoId: String,
    val cerradoEn: Long,
    val totalPosberry: Long,
    val nota: String? = null,
    val sincronizado: Boolean = false,
    val digitalCargado: String = "{}",
    val digitalEditado: String = "{}",
    val ventasDigitalesCargadas: Int = 0,
    val ventasDigitales: Int = 0,
    val totalDigitalCargado: Long = 0,
    val totalDigital: Long = 0,
    val diferenciaDigital: Long = 0,
    val ventasEfectivo: Int = 0,
    val totalVentasEfectivo: Long = 0,
    val totalRetiros: Long = 0,
    val totalProveedores: Long = 0
)
