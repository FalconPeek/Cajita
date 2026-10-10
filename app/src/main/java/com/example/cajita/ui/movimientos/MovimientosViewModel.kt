package com.example.cajita.ui.movimientos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.ProveedorLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.local.entity.VentaLocal
import com.example.cajita.data.repository.CajitaRepository
import com.example.cajita.domain.model.MedioPago
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class MovimientoItem {
    abstract val id: String
    abstract val ocurridoEn: Long

    data class Venta(val venta: VentaLocal) : MovimientoItem() {
        override val id: String get() = venta.id
        override val ocurridoEn: Long get() = venta.ocurridoEn
    }

    data class Egreso(val egreso: EgresoLocal) : MovimientoItem() {
        override val id: String get() = egreso.id
        override val ocurridoEn: Long get() = egreso.ocurridoEn
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CajitaRepository(application)

    val turnoAbierto: StateFlow<TurnoLocal?> = repository.turnoAbiertoFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val conteos = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getConteosFlow(turno.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _filtroSeleccionado = MutableStateFlow("Todos") // "Todos", "Efectivo", "Digital", "Salidas"
    val filtroSeleccionado: StateFlow<String> = _filtroSeleccionado.asStateFlow()

    private val ventasActivas = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getVentasActivasFlow(turno.id) else flowOf(emptyList())
    }

    private val egresosActivos = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getEgresosActivosFlow(turno.id) else flowOf(emptyList())
    }

    val proveedoresRecientes: StateFlow<List<ProveedorLocal>> = repository.getProveedoresRecientesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val listaMovimientosCombined: StateFlow<List<MovimientoItem>> = combine(
        ventasActivas,
        egresosActivos,
        filtroSeleccionado
    ) { ventas, egresos, filtro ->
        val vItems = ventas.map { MovimientoItem.Venta(it) }
        val eItems = egresos.map { MovimientoItem.Egreso(it) }
        val todos = (vItems + eItems).sortedByDescending { it.ocurridoEn }

        when (filtro) {
            "Efectivo" -> todos.filter {
                when (it) {
                    is MovimientoItem.Venta -> it.venta.medio == MedioPago.EFECTIVO || it.venta.efectivoEntrada > 0 || it.venta.efectivoSalida > 0
                    is MovimientoItem.Egreso -> false
                }
            }
            "Digital" -> todos.filter {
                when (it) {
                    is MovimientoItem.Venta -> it.venta.medio != MedioPago.EFECTIVO
                    is MovimientoItem.Egreso -> false
                }
            }
            "Salidas" -> todos.filter {
                when (it) {
                    is MovimientoItem.Venta -> it.venta.efectivoSalida > 0
                    is MovimientoItem.Egreso -> true
                }
            }
            else -> todos
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalesSummary: StateFlow<Triple<Long, Long, Long>> = combine(
        ventasActivas,
        egresosActivos
    ) { ventas, egresos ->
        val totalDigital = ventas.filter { it.medio != MedioPago.EFECTIVO }.sumOf { it.montoDigital }
        val totalEfectivoVentas = ventas.sumOf { it.efectivoEntrada - it.efectivoSalida }
        val totalSalidas = egresos.sumOf { it.monto }

        Triple(totalDigital, totalEfectivoVentas, totalSalidas)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Triple(0L, 0L, 0L)
    )

    val totalOperacionesCount: StateFlow<Int> = combine(
        ventasActivas,
        egresosActivos
    ) { ventas, egresos ->
        ventas.size + egresos.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun seleccionarFiltro(filtro: String) {
        _filtroSeleccionado.value = filtro
    }

    fun borrarVenta(venta: VentaLocal) {
        viewModelScope.launch {
            repository.borrarVenta(venta)
        }
    }

    fun borrarEgreso(egreso: EgresoLocal) {
        viewModelScope.launch {
            repository.borrarEgreso(egreso)
        }
    }

    fun editarVenta(
        ventaActual: VentaLocal,
        nuevoMedio: String,
        nuevoTotalVenta: Long,
        nuevoMontoDigital: Long,
        nuevosBilletesEntrada: Map<Int, Int>,
        nuevosBilletesSalida: Map<Int, Int>,
        nuevaNota: String?
    ) {
        viewModelScope.launch {
            repository.editarVenta(
                ventaActual = ventaActual,
                nuevoMedio = nuevoMedio,
                nuevoTotalVenta = nuevoTotalVenta,
                nuevoMontoDigital = nuevoMontoDigital,
                nuevosBilletesEntrada = nuevosBilletesEntrada,
                nuevosBilletesSalida = nuevosBilletesSalida,
                nuevaNota = nuevaNota
            )
        }
    }

    fun editarEgreso(
        egresoActual: EgresoLocal,
        nuevoTipo: String,
        nuevoOrigen: String,
        nuevosBilletes: Map<Int, Int>,
        nuevoProveedor: String?,
        nuevaNota: String?
    ) {
        viewModelScope.launch {
            repository.editarEgreso(
                egresoActual = egresoActual,
                nuevoTipo = nuevoTipo,
                nuevoOrigen = nuevoOrigen,
                nuevosBilletes = nuevosBilletes,
                nuevoProveedor = nuevoProveedor,
                nuevaNota = nuevaNota
            )
        }
    }
}
