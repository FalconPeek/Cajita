package com.example.cajita.domain.model

sealed class SyncStatus {
    data object Enviado : SyncStatus()
    data class SinConexion(val pendientes: Int) : SyncStatus()
}
