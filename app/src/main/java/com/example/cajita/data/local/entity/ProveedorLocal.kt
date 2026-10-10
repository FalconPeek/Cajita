package com.example.cajita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class ProveedorLocal(
    @PrimaryKey
    val nombre: String,
    val ultimoUso: Long = System.currentTimeMillis()
)
