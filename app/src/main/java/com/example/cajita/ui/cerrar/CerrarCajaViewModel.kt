package com.example.cajita.ui.cerrar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajita.data.local.entity.ConteoLocal
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.local.entity.VentaLocal
import com.example.cajita.data.repository.CajitaRepository
import com.example.cajita.domain.model.MedioPago
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DigitalRowState(
    val ventasCargadas: Int,
    val montoCargado: Long,
    val ventasEditadas: Int,
    val montoEditado: Long,
    val editadoManualmente: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class CerrarCajaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CajitaRepository(application)

    val turnoAbierto: StateFlow<TurnoLocal?> = repository.turnoAbiertoFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val conteos: StateFlow<List<ConteoLocal>> = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getConteosFlow(turno.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val ventasActivas: StateFlow<List<VentaLocal>> = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getVentasActivasFlow(turno.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val egresosActivos: StateFlow<List<EgresoLocal>> = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getEgresosActivosFlow(turno.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _digitalRows = MutableStateFlow<Map<String, DigitalRowState>>(emptyMap())
    val digitalRows: StateFlow<Map<String, DigitalRowState>> = _digitalRows.asStateFlow()

    private val _pasoActual = MutableStateFlow(1) // 1: Digitales, 2: Efectivo, 3: Confirmación
    val pasoActual: StateFlow<Int> = _pasoActual.asStateFlow()

    private val _posberryTexto = MutableStateFlow("")
    val posberryTexto: StateFlow<String> = _posberryTexto.asStateFlow()

    private val _nota = MutableStateFlow("")
    val nota: StateFlow<String> = _nota.asStateFlow()

    private val _mostrarTodosBilletes = MutableStateFlow(false)
    val mostrarTodosBilletes: StateFlow<Boolean> = _mostrarTodosBilletes.asStateFlow()

    init {
        viewModelScope.launch {
            ventasActivas.collect { lista ->
                val currentMap = _digitalRows.value.toMutableMap()
                listOf(MedioPago.DEBITO, MedioPago.QR_TRANSFERENCIA, MedioPago.CREDITO).forEach { m ->
                    val mList = lista.filter { it.medio == m }
                    val realCount = mList.size
                    val realMonto = mList.sumOf { it.montoDigital }
                    val currentState = currentMap[m]

                    if (currentState == null || !currentState.editadoManualmente) {
                        currentMap[m] = DigitalRowState(
                            ventasCargadas = realCount,
                            montoCargado = realMonto,
                            ventasEditadas = realCount,
                            montoEditado = realMonto,
                            editadoManualmente = false
                        )
                    } else {
                        currentMap[m] = currentState.copy(
                            ventasCargadas = realCount,
                            montoCargado = realMonto
                        )
                    }
                }
                _digitalRows.value = currentMap
            }
        }
    }

    fun irAPaso(paso: Int) {
        _pasoActual.value = paso.coerceIn(1, 3)
    }

    fun editarFilaDigital(medio: String, nuevasVentas: Int, nuevoMonto: Long) {
        val actual = _digitalRows.value[medio] ?: return
        val map = _digitalRows.value.toMutableMap()
        map[medio] = actual.copy(
            ventasEditadas = nuevasVentas.coerceAtLeast(0),
            montoEditado = nuevoMonto.coerceAtLeast(0),
            editadoManualmente = true
        )
        _digitalRows.value = map
    }

    fun onPosberryChanged(nuevo: String) {
        _posberryTexto.value = nuevo.filter { it.isDigit() }
    }

    fun onNotaChanged(nueva: String) {
        _nota.value = nueva
    }

    fun toggleMostrarTodosBilletes() {
        _mostrarTodosBilletes.value = !_mostrarTodosBilletes.value
    }

    fun calcularTotalContado(listaConteos: List<ConteoLocal> = conteos.value): Long {
        var total = 0L
        for (c in listaConteos) {
            total += c.billetesCaja.toLong() * c.denominacion.toLong()
            total += c.toquitosCajita.toLong() * 10L * c.denominacion.toLong()
        }
        return total
    }

    fun calcularTotalCaja(): Long {
        return conteos.value.sumOf { it.billetesCaja.toLong() * it.denominacion.toLong() }
    }

    fun calcularTotalCajita(): Long {
        return conteos.value.sumOf { it.toquitosCajita.toLong() * 10L * it.denominacion.toLong() }
    }

    fun calcularTotalToquitos(): Int {
        return conteos.value.sumOf { it.toquitosCajita }
    }

    fun calcularPosberryLong(): Long {
        return _posberryTexto.value.toLongOrNull() ?: 0L
    }

    fun confirmarCierre(onComplete: (String) -> Unit) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            val rows = _digitalRows.value
            val vDigitalesCargadas = rows.values.sumOf { it.ventasCargadas }
            val vDigitalesEditadas = rows.values.sumOf { it.ventasEditadas }
            val tDigitalCargado = rows.values.sumOf { it.montoCargado }
            val tDigitalEditado = rows.values.sumOf { it.montoEditado }
            val diffDigital = tDigitalEditado - tDigitalCargado

            val vEfectivoList = ventasActivas.value.filter { it.medio == MedioPago.EFECTIVO }
            val vEfectivoCount = vEfectivoList.size
            val vEfectivoMonto = vEfectivoList.sumOf { it.totalVenta }

            val egresosList = egresosActivos.value
            val retirosMonto = egresosList.filter { it.tipo == "RETIRO" }.sumOf { it.monto }
            val proveedoresMonto = egresosList.filter { it.tipo == "PROVEEDOR" }.sumOf { it.monto }

            val digitalCargadoJson = buildDigitalMapJson(rows) { it.ventasCargadas to it.montoCargado }
            val digitalEditadoJson = buildDigitalMapJson(rows) { it.ventasEditadas to it.montoEditado }

            val cierreId = repository.cerrarTurno(
                turnoId = turnoId,
                posberry = calcularPosberryLong(),
                nota = _nota.value,
                digitalCargadoJson = digitalCargadoJson,
                digitalEditadoJson = digitalEditadoJson,
                ventasDigitalesCargadas = vDigitalesCargadas,
                ventasDigitales = vDigitalesEditadas,
                totalDigitalCargado = tDigitalCargado,
                totalDigital = tDigitalEditado,
                diferenciaDigital = diffDigital,
                ventasEfectivo = vEfectivoCount,
                totalVentasEfectivo = vEfectivoMonto,
                totalRetiros = retirosMonto,
                totalProveedores = proveedoresMonto
            )
            onComplete(cierreId)
        }
    }

    private fun buildDigitalMapJson(
        rows: Map<String, DigitalRowState>,
        valueSelector: (DigitalRowState) -> Pair<Int, Long>
    ): String {
        val mapStr = rows.entries.joinToString(",") { (medio, state) ->
            val (v, m) = valueSelector(state)
            "\"$medio\":{\"ventas\":$v,\"monto\":$m}"
        }
        return "{$mapStr}"
    }
}
