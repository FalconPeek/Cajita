package com.example.cajita.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajita.data.local.entity.CierreLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.repository.CajitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CajitaRepository(application)

    val turnoAbierto: StateFlow<TurnoLocal?> = repository.turnoAbiertoFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val ultimoCierre: StateFlow<CierreLocal?> = repository.ultimoCierreFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _cajeroUltimoCierre = MutableStateFlow<String?>(null)
    val cajeroUltimoCierre: StateFlow<String?> = _cajeroUltimoCierre.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        sincronizarConSupabase()

        viewModelScope.launch {
            ultimoCierre.collect { cierre ->
                if (cierre != null) {
                    val turno = repository.getTurnoById(cierre.turnoId)
                    _cajeroUltimoCierre.value = turno?.cajero
                } else {
                    _cajeroUltimoCierre.value = null
                }
            }
        }
    }

    fun sincronizarConSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.sincronizarDesdeSupabase()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
