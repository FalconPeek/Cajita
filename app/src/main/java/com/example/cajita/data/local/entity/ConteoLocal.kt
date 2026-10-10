package com.example.cajita.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "conteos",
    primaryKeys = ["turnoId", "denominacion"]
)
data class ConteoLocal(
    val turnoId: String,
    val denominacion: Int,
    val billetesCaja: Int,
    val toquitosCajita: Int
)
