package com.example.cajita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "egresos")
data class EgresoLocal(
    @PrimaryKey
    val id: String,
    val egresoId: String,
    val version: Int = 1,
    val eliminado: Boolean = false,
    val turnoId: String,
    val ocurridoEn: Long,
    val registradoEn: Long = System.currentTimeMillis(),
    val tipo: String, // "RETIRO", "PROVEEDOR"
    val origen: String, // "CAJA", "CAJITA"
    val monto: Long,
    val billetes: String, // JSON String map "{"2000": 1}"
    val proveedor: String? = null,
    val nota: String? = null,
    val sincronizado: Boolean = false
)
