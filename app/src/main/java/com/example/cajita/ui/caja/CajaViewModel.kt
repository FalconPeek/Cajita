package com.example.cajita.ui.caja

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajita.data.local.entity.ConteoLocal
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.ProveedorLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.local.entity.VentaLocal
import com.example.cajita.data.repository.CajitaRepository
import com.example.cajita.domain.model.MedioPago
import com.example.cajita.domain.model.SyncStatus
import com.example.cajita.domain.model.toFormatoMonedaARS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CajaViewModel(application: Application) : AndroidViewModel(application) {

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

    val proveedoresRecientes: StateFlow<List<ProveedorLocal>> = repository.getProveedoresRecientesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val operacionesCount: StateFlow<Int> = turnoAbierto.flatMapLatest { turno ->
        if (turno != null) repository.getOperacionesCountFlow(turno.id) else flowOf(0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val syncStatus: StateFlow<SyncStatus> = repository.pendingSyncCountFlow.map { pendientes ->
        if (pendientes == 0) SyncStatus.Enviado else SyncStatus.SinConexion(pendientes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncStatus.Enviado
    )

    private val _ultimoMensajeUndo = MutableStateFlow<String?>(null)
    val ultimoMensajeUndo: StateFlow<String?> = _ultimoMensajeUndo.asStateFlow()

    private var ultimaVentaCreada: VentaLocal? = null
    private var ultimoEgresoCreado: EgresoLocal? = null

    private val _denominacionAEditar = MutableStateFlow<Int?>(null)
    val denominacionAEditar: StateFlow<Int?> = _denominacionAEditar.asStateFlow()

    fun modificarBilletes(denominacion: Int, delta: Int) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            repository.modificarBilletes(turnoId, denominacion, delta)
            val signo = if (delta > 0) "+$delta" else "$delta"
            _ultimoMensajeUndo.value = "$signo billete de $ $denominacion"
            ultimaVentaCreada = null
            ultimoEgresoCreado = null
        }
    }

    fun moverACajita(denominacion: Int) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            repository.moverACajita(turnoId, denominacion)
            _ultimoMensajeUndo.value = "Pasado 1 toquito de $ $denominacion a cajita"
            ultimaVentaCreada = null
            ultimoEgresoCreado = null
        }
    }

    fun sacarDeCajita(denominacion: Int) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            repository.sacarDeCajita(turnoId, denominacion)
            _ultimoMensajeUndo.value = "Sacado 1 toquito de $ $denominacion de cajita"
            ultimaVentaCreada = null
            ultimoEgresoCreado = null
        }
    }

    fun fijarCantidad(denominacion: Int, nuevaCantidad: Int) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            repository.fijarCantidadBilletes(turnoId, denominacion, nuevaCantidad)
            _ultimoMensajeUndo.value = "Editado billetes de $ $denominacion a $nuevaCantidad"
            ultimaVentaCreada = null
            ultimoEgresoCreado = null
        }
    }

    fun guardarNuevaVenta(
        medio: String,
        totalVenta: Long,
        montoDigital: Long,
        billetesEntrada: Map<Int, Int>,
        billetesSalida: Map<Int, Int>,
        nota: String?
    ) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            try {
                val venta = repository.guardarNuevaVenta(
                    turnoId = turnoId,
                    medio = medio,
                    totalVenta = totalVenta,
                    montoDigital = montoDigital,
                    billetesEntrada = billetesEntrada,
                    billetesSalida = billetesSalida,
                    nota = nota
                )
                ultimaVentaCreada = venta
                ultimoEgresoCreado = null

                val labelMedio = if (medio == MedioPago.EFECTIVO) "Efectivo" else MedioPago.toLabel(medio).split("/")[0].trim()
                val textoDetalle = when {
                    medio == MedioPago.EFECTIVO && venta.efectivoSalida > 0 -> " · salió ${venta.efectivoSalida.toFormatoMonedaARS()}"
                    venta.montoDigital > 0 && venta.efectivoSalida > 0 -> " · salió ${venta.efectivoSalida.toFormatoMonedaARS()}"
                    venta.montoDigital > 0 && venta.efectivoEntrada > 0 -> " · entró ${venta.efectivoEntrada.toFormatoMonedaARS()}"
                    else -> ""
                }
                _ultimoMensajeUndo.value = "Venta $labelMedio ${totalVenta.toFormatoMonedaARS()}$textoDetalle"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun guardarNuevoEgreso(
        tipo: String,
        origen: String,
        billetesCaja: Map<Int, Int>,
        toquitosCajita: Map<Int, Int>,
        proveedor: String?,
        nota: String?
    ) {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            try {
                val egreso = repository.guardarNuevoEgreso(
                    turnoId = turnoId,
                    tipo = tipo,
                    origen = origen,
                    billetesCaja = billetesCaja,
                    toquitosCajita = toquitosCajita,
                    proveedor = proveedor,
                    nota = nota
                )
                ultimoEgresoCreado = egreso
                ultimaVentaCreada = null

                val labelTipo = if (tipo == "PROVEEDOR") "Pago a proveedor" else "Retiro"
                _ultimoMensajeUndo.value = "$labelTipo ${egreso.monto.toFormatoMonedaARS()}"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deshacer() {
        val turnoId = turnoAbierto.value?.id ?: return
        viewModelScope.launch {
            when {
                ultimaVentaCreada != null -> {
                    repository.borrarVenta(ultimaVentaCreada!!)
                    ultimaVentaCreada = null
                }
                ultimoEgresoCreado != null -> {
                    repository.borrarEgreso(ultimoEgresoCreado!!)
                    ultimoEgresoCreado = null
                }
                else -> {
                    repository.deshacerUltimoMovimiento(turnoId)
                }
            }
            _ultimoMensajeUndo.value = null
        }
    }

    fun limpiarMensajeUndo() {
        _ultimoMensajeUndo.value = null
    }

    fun abrirEditarModal(denominacion: Int) {
        _denominacionAEditar.value = denominacion
    }

    fun cerrarEditarModal() {
        _denominacionAEditar.value = null
    }

    fun calcularTotales(): Triple<Long, Long, Long> {
        var totalCaja = 0L
        var totalCajita = 0L

        for (c in conteos.value) {
            totalCaja += c.billetesCaja.toLong() * c.denominacion.toLong()
            totalCajita += c.toquitosCajita.toLong() * 10L * c.denominacion.toLong()
        }

        return Triple(totalCaja, totalCajita, totalCaja + totalCajita)
    }

    fun calcularTotalToquitosCount(): Int {
        return conteos.value.sumOf { it.toquitosCajita }
    }
}
