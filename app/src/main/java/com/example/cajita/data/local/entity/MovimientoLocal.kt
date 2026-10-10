package com.example.cajita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class MovimientoLocal(
    @PrimaryKey
    val id: String,
    val turnoId: String,
    val ocurridoEn: Long,
    val tipo: String, // "suma", "resta", "editar", "a_cajita", "desde_cajita"
    val denominacion: Int,
    val cantidad: Int,
    val sincronizado: Boolean = false
)
