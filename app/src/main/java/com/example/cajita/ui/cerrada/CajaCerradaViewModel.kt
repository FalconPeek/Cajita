package com.example.cajita.ui.cerrada

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajita.data.local.entity.CierreLocal
import com.example.cajita.data.local.entity.ConteoLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.repository.CajitaRepository
import com.example.cajita.domain.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CajaCerradaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CajitaRepository(application)

    private val _cierre = MutableStateFlow<CierreLocal?>(null)
    val cierre: StateFlow<CierreLocal?> = _cierre.asStateFlow()

    private val _turno = MutableStateFlow<TurnoLocal?>(null)
    val turno: StateFlow<TurnoLocal?> = _turno.asStateFlow()

    private val _conteos = MutableStateFlow<List<ConteoLocal>>(emptyList())
    val conteos: StateFlow<List<ConteoLocal>> = _conteos.asStateFlow()

    val syncStatus: StateFlow<SyncStatus> = repository.pendingSyncCountFlow.map { pendientes ->
        if (pendientes == 0) SyncStatus.Enviado else SyncStatus.SinConexion(pendientes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncStatus.Enviado
    )

    fun cargarCierre(cierreId: String) {
        viewModelScope.launch {
            val c = if (cierreId.isNotBlank()) repository.getCierreById(cierreId) else repository.getUltimoCierre()
            _cierre.value = c
            if (c != null) {
                _turno.value = repository.getTurnoById(c.turnoId)
                _conteos.value = repository.getConteosByTurnoId(c.turnoId)
            }
        }
    }

    fun calcularTotalContado(listaConteos: List<ConteoLocal> = conteos.value): Long {
        var total = 0L
        for (c in listaConteos) {
            total += c.billetesCaja.toLong() * c.denominacion.toLong()
            total += c.toquitosCajita.toLong() * 10L * c.denominacion.toLong()
        }
        return total
    }
}
