package com.example.cajita.ui.abrir

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajita.data.repository.CajitaRepository
import com.example.cajita.domain.model.Denominaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AbrirTurnoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CajitaRepository(application)

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _posberryTexto = MutableStateFlow("")
    val posberryTexto: StateFlow<String> = _posberryTexto.asStateFlow()

    private val _tabSeleccionada = MutableStateFlow(0) // 0 = Caja (billetes), 1 = Cajita (toquitos)
    val tabSeleccionada: StateFlow<Int> = _tabSeleccionada.asStateFlow()

    // Map: denominacion -> Pair(billetesCaja, toquitosCajita)
    private val _conteos = MutableStateFlow<Map<Int, Pair<Int, Int>>>(emptyMap())
    val conteos: StateFlow<Map<Int, Pair<Int, Int>>> = _conteos.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ultimoCajeroFlow.collect { ultimo ->
                if (ultimo.isNotBlank()) {
                    _nombre.value = ultimo
                }
            }
        }
        val conteoInicialDefault = Denominaciones.LISTA.associate { item ->
            item.valor to Pair(0, 0)
        }
        _conteos.value = conteoInicialDefault
    }

    fun onNombreChanged(nuevo: String) {
        _nombre.value = nuevo
    }

    fun onPosberryChanged(nuevo: String) {
        _posberryTexto.value = nuevo.filter { it.isDigit() }
    }

    fun onTabChanged(index: Int) {
        _tabSeleccionada.value = index
    }

    fun modificarConteo(denominacion: Int, delta: Int) {
        val actualMap = _conteos.value.toMutableMap()
        val pairActual = actualMap[denominacion] ?: Pair(0, 0)
        val tab = _tabSeleccionada.value

        val nuevoPair = if (tab == 0) {
            val nuevosBilletes = (pairActual.first + delta).coerceAtLeast(0)
            Pair(nuevosBilletes, pairActual.second)
        } else {
            val nuevosToquitos = (pairActual.second + delta).coerceAtLeast(0)
            Pair(pairActual.first, nuevosToquitos)
        }

        actualMap[denominacion] = nuevoPair
        _conteos.value = actualMap
    }

    fun usarConteoDelCierreAnterior() {
        viewModelScope.launch {
            val conteosAnteriores = repository.getConteosDelUltimoCierre()
            if (conteosAnteriores.isNotEmpty()) {
                _conteos.value = conteosAnteriores
            }
        }
    }

    fun calcularTotalContado(mapaConteos: Map<Int, Pair<Int, Int>> = _conteos.value): Long {
        var total = 0L
        for ((denominacion, pair) in mapaConteos) {
            val (billetes, toquitos) = pair
            total += billetes.toLong() * denominacion.toLong()
            total += toquitos.toLong() * 10L * denominacion.toLong()
        }
        return total
    }

    fun calcularPosberryLong(): Long {
        return _posberryTexto.value.toLongOrNull() ?: 0L
    }

    fun empezarTurno(onComplete: () -> Unit) {
        viewModelScope.launch {
            val posberry = calcularPosberryLong()
            repository.abrirTurno(
                cajeroNombre = _nombre.value,
                montoPosberry = posberry,
                conteosIniciales = _conteos.value
            )
            onComplete()
        }
    }
}
