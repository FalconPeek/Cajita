package com.example.cajita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaLocal(
    @PrimaryKey
    val id: String,
    val ventaId: String,
    val version: Int = 1,
    val eliminada: Boolean = false,
    val turnoId: String,
    val ocurridoEn: Long,
    val registradoEn: Long = System.currentTimeMillis(),
    val medio: String, // 'efectivo', 'debito', 'qr_transferencia', 'credito'
    val totalVenta: Long,
    val montoDigital: Long,
    val efectivoEntrada: Long,
    val efectivoSalida: Long,
    val billetesEntrada: String, // JSON String map "{"2000": 3}"
    val billetesSalida: String,  // JSON String map "{"1000": 1}"
    val nota: String? = null,
    val sincronizado: Boolean = false
)
