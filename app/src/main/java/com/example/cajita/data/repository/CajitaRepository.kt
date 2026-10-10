package com.example.cajita.data.repository

import android.content.Context
import com.example.cajita.data.local.dao.CajitaDao
import com.example.cajita.data.local.db.CajitaDatabase
import com.example.cajita.data.local.entity.CierreLocal
import com.example.cajita.data.local.entity.ConteoLocal
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.MovimientoLocal
import com.example.cajita.data.local.entity.ProveedorLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.local.entity.VentaLocal
import com.example.cajita.data.preferences.UserPreferencesRepository
import com.example.cajita.data.remote.SupabaseService
import com.example.cajita.data.worker.Sincronizador
import com.example.cajita.data.worker.SyncWorker
import com.example.cajita.domain.model.Denominaciones
import com.example.cajita.domain.model.VentaLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class CajitaRepository(private val context: Context) {

    private val db = CajitaDatabase.getDatabase(context)
    private val dao = db.cajitaDao()
    private val userPrefs = UserPreferencesRepository(context)
    private val supabaseService = SupabaseService()

    init {
        SyncWorker.enqueuePeriodic(context)
    }

    val turnoAbiertoFlow: Flow<TurnoLocal?> = dao.getTurnoAbiertoFlow()
    val ultimoCierreFlow: Flow<CierreLocal?> = dao.getUltimoCierreFlow()
    val pendingSyncCountFlow: Flow<Int> = dao.getPendingSyncCountFlow()
    val ultimoCajeroFlow: Flow<String> = userPrefs.ultimoCajeroFlow

    fun getConteosFlow(turnoId: String): Flow<List<ConteoLocal>> {
        return dao.getConteosFlow(turnoId)
    }

    fun getVentasActivasFlow(turnoId: String): Flow<List<VentaLocal>> {
        return dao.getVentasActivasFlow(turnoId)
    }

    fun getEgresosActivosFlow(turnoId: String): Flow<List<EgresoLocal>> {
        return dao.getEgresosActivosFlow(turnoId)
    }

    fun getProveedoresRecientesFlow(): Flow<List<ProveedorLocal>> {
        return dao.getProveedoresRecientesFlow()
    }

    fun getOperacionesCountFlow(turnoId: String): Flow<Int> {
        return dao.getOperacionesCountFlow(turnoId)
    }

    suspend fun getOrCreateDispositivoId(): String {
        return userPrefs.getOrCreateDispositivoId()
    }

    suspend fun saveUltimoCajero(nombre: String) {
        userPrefs.saveUltimoCajero(nombre)
    }

    fun getCierreByIdFlow(id: String): Flow<CierreLocal?> {
        return dao.getCierreByIdFlow(id)
    }

    suspend fun getCierreById(id: String): CierreLocal? {
        return dao.getCierreById(id)
    }

    suspend fun getUltimoCierre(): CierreLocal? {
        return dao.getUltimoCierre()
    }

    suspend fun getTurnoById(id: String): TurnoLocal? {
        return dao.getTurnoById(id)
    }

    suspend fun getConteosByTurnoId(turnoId: String): List<ConteoLocal> {
        return dao.getConteos(turnoId)
    }

    suspend fun sincronizarDesdeSupabase() {
        try {
            val ultimoCierreDto = supabaseService.fetchUltimoCierre()
            if (ultimoCierreDto != null) {
                val cerradoEnMillis = try {
                    Instant.parse(ultimoCierreDto.cerradoEn).toEpochMilli()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }

                var turnoLocal = dao.getTurnoById(ultimoCierreDto.turnoId)
                if (turnoLocal == null) {
                    val remoteTurno = supabaseService.fetchTurnoById(ultimoCierreDto.turnoId)
                    turnoLocal = TurnoLocal(
                        id = ultimoCierreDto.turnoId,
                        cajero = remoteTurno?.cajero,
                        abiertoEn = remoteTurno?.abiertoEn?.let {
                            try { Instant.parse(it).toEpochMilli() } catch (_: Exception) { cerradoEnMillis - 28800000L }
                        } ?: (cerradoEnMillis - 28800000L),
                        montoAperturaDeclarado = remoteTurno?.montoAperturaDeclarado ?: ultimoCierreDto.totalPosberry,
                        estado = "CERRADO",
                        sincronizadoApertura = true,
                        sincronizadoCierre = true
                    )
                    dao.insertTurno(turnoLocal)
                } else if (turnoLocal.cajero.isNullOrBlank()) {
                    val remoteTurno = supabaseService.fetchTurnoById(ultimoCierreDto.turnoId)
                    if (!remoteTurno?.cajero.isNullOrBlank()) {
                        dao.updateTurno(turnoLocal.copy(cajero = remoteTurno.cajero))
                    }
                }

                val cierreLocal = CierreLocal(
                    id = ultimoCierreDto.id,
                    turnoId = ultimoCierreDto.turnoId,
                    cerradoEn = cerradoEnMillis,
                    totalPosberry = ultimoCierreDto.totalPosberry,
                    nota = ultimoCierreDto.nota,
                    sincronizado = true,
                    digitalCargado = ultimoCierreDto.digitalCargado?.toString() ?: "{}",
                    digitalEditado = ultimoCierreDto.digitalEditado?.toString() ?: "{}",
                    ventasDigitalesCargadas = ultimoCierreDto.ventasDigitalesCargadas,
                    ventasDigitales = ultimoCierreDto.ventasDigitales,
                    totalDigitalCargado = ultimoCierreDto.totalDigitalCargado,
                    totalDigital = ultimoCierreDto.totalDigital,
                    diferenciaDigital = ultimoCierreDto.diferenciaDigital,
                    ventasEfectivo = ultimoCierreDto.ventasEfectivo,
                    totalVentasEfectivo = ultimoCierreDto.totalVentasEfectivo,
                    totalRetiros = ultimoCierreDto.totalRetiros,
                    totalProveedores = ultimoCierreDto.totalProveedores
                )

                val conteosLocal = Denominaciones.LISTA.map { item ->
                    val key = item.valor.toString()
                    val billetes = ultimoCierreDto.billetesCaja[key]?.let {
                        it.toString().removeSurrounding("\"").toIntOrNull() ?: 0
                    } ?: 0
                    val toquitos = ultimoCierreDto.toquitosCajita[key]?.let {
                        it.toString().removeSurrounding("\"").toIntOrNull() ?: 0
                    } ?: 0
                    ConteoLocal(
                        turnoId = ultimoCierreDto.turnoId,
                        denominacion = item.valor,
                        billetesCaja = billetes,
                        toquitosCajita = toquitos
                    )
                }

                dao.insertCierre(cierreLocal)
                dao.insertConteos(conteosLocal)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun abrirTurno(cajeroNombre: String?, montoPosberry: Long, conteosIniciales: Map<Int, Pair<Int, Int>>): String {
        val cajeroLimpio = cajeroNombre?.trim() ?: ""
        saveUltimoCajero(cajeroLimpio)

        val turnoId = UUID.randomUUID().toString()
        val nuevoTurno = TurnoLocal(
            id = turnoId,
            cajero = cajeroLimpio,
            abiertoEn = System.currentTimeMillis(),
            montoAperturaDeclarado = montoPosberry,
            estado = "ABIERTO",
            sincronizadoApertura = false,
            sincronizadoCierre = false
        )

        val listaConteos = Denominaciones.LISTA.map { item ->
            val pair = conteosIniciales[item.valor] ?: Pair(0, 0)
            ConteoLocal(
                turnoId = turnoId,
                denominacion = item.valor,
                billetesCaja = pair.first.coerceAtLeast(0),
                toquitosCajita = pair.second.coerceAtLeast(0)
            )
        }

        dao.insertTurno(nuevoTurno)
        dao.insertConteos(listaConteos)

        triggerSync()
        return turnoId
    }

    suspend fun getConteosDelUltimoCierre(): Map<Int, Pair<Int, Int>> {
        val ultimoCierre = dao.getUltimoCierre() ?: return emptyMap()
        val conteosAnteriores = dao.getConteos(ultimoCierre.turnoId)
        return conteosAnteriores.associate { c ->
            c.denominacion to Pair(c.billetesCaja, c.toquitosCajita)
        }
    }

    suspend fun modificarBilletes(turnoId: String, denominacion: Int, delta: Int) {
        val conteoActual = dao.getConteoByDenominacion(turnoId, denominacion) ?: ConteoLocal(
            turnoId = turnoId,
            denominacion = denominacion,
            billetesCaja = 0,
            toquitosCajita = 0
        )

        val nuevaCantidad = (conteoActual.billetesCaja + delta).coerceAtLeast(0)
        val deltaReal = nuevaCantidad - conteoActual.billetesCaja
        if (deltaReal == 0) return

        val tipoMovimiento = if (deltaReal > 0) "suma" else "resta"
        val conteoActualizado = conteoActual.copy(billetesCaja = nuevaCantidad)

        dao.insertOrUpdateConteo(conteoActualizado)

        val movimiento = MovimientoLocal(
            id = UUID.randomUUID().toString(),
            turnoId = turnoId,
            ocurridoEn = System.currentTimeMillis(),
            tipo = tipoMovimiento,
            denominacion = denominacion,
            cantidad = Math.abs(deltaReal),
            sincronizado = false
        )
        dao.insertMovimiento(movimiento)

        triggerSync()
    }

    suspend fun fijarCantidadBilletes(turnoId: String, denominacion: Int, nuevaCantidad: Int) {
        val conteoActual = dao.getConteoByDenominacion(turnoId, denominacion) ?: ConteoLocal(
            turnoId = turnoId,
            denominacion = denominacion,
            billetesCaja = 0,
            toquitosCajita = 0
        )

        val cantidadSegura = nuevaCantidad.coerceAtLeast(0)
        if (conteoActual.billetesCaja == cantidadSegura) return

        val conteoActualizado = conteoActual.copy(billetesCaja = cantidadSegura)
        dao.insertOrUpdateConteo(conteoActualizado)

        val movimiento = MovimientoLocal(
            id = UUID.randomUUID().toString(),
            turnoId = turnoId,
            ocurridoEn = System.currentTimeMillis(),
            tipo = "editar",
            denominacion = denominacion,
            cantidad = cantidadSegura,
            sincronizado = false
        )
        dao.insertMovimiento(movimiento)

        triggerSync()
    }

    suspend fun moverACajita(turnoId: String, denominacion: Int) {
        val conteoActual = dao.getConteoByDenominacion(turnoId, denominacion) ?: return
        if (conteoActual.billetesCaja < Denominaciones.BILLETES_POR_TOQUITO) return

        val nuevoConteo = conteoActual.copy(
            billetesCaja = conteoActual.billetesCaja - Denominaciones.BILLETES_POR_TOQUITO,
            toquitosCajita = conteoActual.toquitosCajita + 1
        )
        dao.insertOrUpdateConteo(nuevoConteo)

        val movimiento = MovimientoLocal(
            id = UUID.randomUUID().toString(),
            turnoId = turnoId,
            ocurridoEn = System.currentTimeMillis(),
            tipo = "a_cajita",
            denominacion = denominacion,
            cantidad = 1,
            sincronizado = false
        )
        dao.insertMovimiento(movimiento)

        triggerSync()
    }

    suspend fun sacarDeCajita(turnoId: String, denominacion: Int) {
        val conteoActual = dao.getConteoByDenominacion(turnoId, denominacion) ?: return
        if (conteoActual.toquitosCajita <= 0) return

        val nuevoConteo = conteoActual.copy(
            billetesCaja = conteoActual.billetesCaja + Denominaciones.BILLETES_POR_TOQUITO,
            toquitosCajita = conteoActual.toquitosCajita - 1
        )
        dao.insertOrUpdateConteo(nuevoConteo)

        val movimiento = MovimientoLocal(
            id = UUID.randomUUID().toString(),
            turnoId = turnoId,
            ocurridoEn = System.currentTimeMillis(),
            tipo = "desde_cajita",
            denominacion = denominacion,
            cantidad = 1,
            sincronizado = false
        )
        dao.insertMovimiento(movimiento)

        triggerSync()
    }

    suspend fun deshacerUltimoMovimiento(turnoId: String) {
        val ultimoMov = dao.getUltimoMovimiento(turnoId) ?: return
        val conteoActual = dao.getConteoByDenominacion(turnoId, ultimoMov.denominacion) ?: return

        val nuevoConteo = when (ultimoMov.tipo) {
            "suma" -> conteoActual.copy(billetesCaja = (conteoActual.billetesCaja - ultimoMov.cantidad).coerceAtLeast(0))
            "resta" -> conteoActual.copy(billetesCaja = conteoActual.billetesCaja + ultimoMov.cantidad)
            "a_cajita" -> conteoActual.copy(
                billetesCaja = conteoActual.billetesCaja + Denominaciones.BILLETES_POR_TOQUITO * ultimoMov.cantidad,
                toquitosCajita = (conteoActual.toquitosCajita - ultimoMov.cantidad).coerceAtLeast(0)
            )
            "desde_cajita" -> conteoActual.copy(
                billetesCaja = (conteoActual.billetesCaja - Denominaciones.BILLETES_POR_TOQUITO * ultimoMov.cantidad).coerceAtLeast(0),
                toquitosCajita = conteoActual.toquitosCajita + ultimoMov.cantidad
            )
            else -> null
        }

        if (nuevoConteo != null) {
            dao.insertOrUpdateConteo(nuevoConteo)
            dao.deleteMovimiento(ultimoMov.id)
            triggerSync()
        }
    }

    // --- Operaciones de Ventas ---

    suspend fun guardarNuevaVenta(
        turnoId: String,
        medio: String,
        totalVenta: Long,
        montoDigital: Long,
        billetesEntrada: Map<Int, Int>,
        billetesSalida: Map<Int, Int>,
        nota: String?
    ): VentaLocal {
        val conteosActuales = dao.getConteos(turnoId).associateBy { it.denominacion }

        for ((denom, cantSalida) in billetesSalida) {
            if (cantSalida <= 0) continue
            val actual = conteosActuales[denom]?.billetesCaja ?: 0
            if (actual < cantSalida) {
                throw IllegalStateException("No hay suficientes billetes de $ $denom en caja para dar el vuelto/salida")
            }
        }

        val efectivoEntrada = VentaLogic.calcularMontoBilletes(billetesEntrada)
        val efectivoSalida = VentaLogic.calcularMontoBilletes(billetesSalida)

        require(VentaLogic.esVentaValida(montoDigital, efectivoEntrada, efectivoSalida, totalVenta)) {
            "La venta no cuadra: $montoDigital digital + $efectivoEntrada entrada - $efectivoSalida salida != $totalVenta total"
        }

        val todasDenoms = Denominaciones.LISTA.map { it.valor }
        for (denom in todasDenoms) {
            val entraron = billetesEntrada[denom] ?: 0
            val salieron = billetesSalida[denom] ?: 0
            val delta = entraron - salieron
            if (delta != 0) {
                val cActual = conteosActuales[denom] ?: ConteoLocal(turnoId = turnoId, denominacion = denom, billetesCaja = 0, toquitosCajita = 0)
                val nuevaCant = (cActual.billetesCaja + delta).coerceAtLeast(0)
                dao.insertOrUpdateConteo(cActual.copy(billetesCaja = nuevaCant))
            }
        }

        val ventaId = UUID.randomUUID().toString()
        val ahora = System.currentTimeMillis()
        val ventaLocal = VentaLocal(
            id = UUID.randomUUID().toString(),
            ventaId = ventaId,
            version = 1,
            eliminada = false,
            turnoId = turnoId,
            ocurridoEn = ahora,
            registradoEn = ahora,
            medio = medio,
            totalVenta = totalVenta,
            montoDigital = montoDigital,
            efectivoEntrada = efectivoEntrada,
            efectivoSalida = efectivoSalida,
            billetesEntrada = VentaLogic.mapToJsonString(billetesEntrada),
            billetesSalida = VentaLogic.mapToJsonString(billetesSalida),
            nota = nota?.ifBlank { null },
            sincronizado = false
        )

        dao.insertVenta(ventaLocal)
        triggerSync()
        return ventaLocal
    }

    suspend fun editarVenta(
        ventaActual: VentaLocal,
        nuevoMedio: String,
        nuevoTotalVenta: Long,
        nuevoMontoDigital: Long,
        nuevosBilletesEntrada: Map<Int, Int>,
        nuevosBilletesSalida: Map<Int, Int>,
        nuevaNota: String?
    ): VentaLocal {
        val conteosActuales = dao.getConteos(ventaActual.turnoId).associateBy { it.denominacion }
        val viejosEntrada = VentaLogic.jsonStringToMap(ventaActual.billetesEntrada)
        val viejosSalida = VentaLogic.jsonStringToMap(ventaActual.billetesSalida)

        val nuevoEfectivoEntrada = VentaLogic.calcularMontoBilletes(nuevosBilletesEntrada)
        val nuevoEfectivoSalida = VentaLogic.calcularMontoBilletes(nuevosBilletesSalida)

        require(VentaLogic.esVentaValida(nuevoMontoDigital, nuevoEfectivoEntrada, nuevoEfectivoSalida, nuevoTotalVenta)) {
            "La venta no cuadra con los datos ingresados"
        }

        val todasDenoms = Denominaciones.LISTA.map { it.valor }
        for (denom in todasDenoms) {
            val viejoEfecto = (viejosEntrada[denom] ?: 0) - (viejosSalida[denom] ?: 0)
            val nuevoEfecto = (nuevosBilletesEntrada[denom] ?: 0) - (nuevosBilletesSalida[denom] ?: 0)
            val delta = nuevoEfecto - viejoEfecto

            if (delta != 0) {
                val cActual = conteosActuales[denom] ?: ConteoLocal(turnoId = ventaActual.turnoId, denominacion = denom, billetesCaja = 0, toquitosCajita = 0)
                val nuevaCant = cActual.billetesCaja + delta
                if (nuevaCant < 0) {
                    throw IllegalStateException("No hay suficientes billetes de $ $denom para aplicar la edición")
                }
                dao.insertOrUpdateConteo(cActual.copy(billetesCaja = nuevaCant))
            }
        }

        val nuevaVersionVenta = ventaActual.copy(
            id = UUID.randomUUID().toString(),
            version = ventaActual.version + 1,
            registradoEn = System.currentTimeMillis(),
            medio = nuevoMedio,
            totalVenta = nuevoTotalVenta,
            montoDigital = nuevoMontoDigital,
            efectivoEntrada = nuevoEfectivoEntrada,
            efectivoSalida = nuevoEfectivoSalida,
            billetesEntrada = VentaLogic.mapToJsonString(nuevosBilletesEntrada),
            billetesSalida = VentaLogic.mapToJsonString(nuevosBilletesSalida),
            nota = nuevaNota?.ifBlank { null },
            sincronizado = false
        )

        dao.insertVenta(nuevaVersionVenta)
        triggerSync()
        return nuevaVersionVenta
    }

    suspend fun borrarVenta(ventaActual: VentaLocal): VentaLocal {
        val conteosActuales = dao.getConteos(ventaActual.turnoId).associateBy { it.denominacion }
        val viejosEntrada = VentaLogic.jsonStringToMap(ventaActual.billetesEntrada)
        val viejosSalida = VentaLogic.jsonStringToMap(ventaActual.billetesSalida)

        val todasDenoms = Denominaciones.LISTA.map { it.valor }
        for (denom in todasDenoms) {
            val viejoEfecto = (viejosEntrada[denom] ?: 0) - (viejosSalida[denom] ?: 0)
            if (viejoEfecto != 0) {
                val cActual = conteosActuales[denom] ?: ConteoLocal(turnoId = ventaActual.turnoId, denominacion = denom, billetesCaja = 0, toquitosCajita = 0)
                val nuevaCant = (cActual.billetesCaja - viejoEfecto).coerceAtLeast(0)
                dao.insertOrUpdateConteo(cActual.copy(billetesCaja = nuevaCant))
            }
        }

        val ventaEliminada = ventaActual.copy(
            id = UUID.randomUUID().toString(),
            version = ventaActual.version + 1,
            eliminada = true,
            registradoEn = System.currentTimeMillis(),
            sincronizado = false
        )

        dao.insertVenta(ventaEliminada)
        triggerSync()
        return ventaEliminada
    }

    // --- Operaciones de Egresos ---

    suspend fun guardarNuevoEgreso(
        turnoId: String,
        tipo: String,
        origen: String,
        billetesCaja: Map<Int, Int>,
        toquitosCajita: Map<Int, Int>,
        proveedor: String?,
        nota: String?
    ): EgresoLocal {
        val conteosActuales = dao.getConteos(turnoId).associateBy { it.denominacion }

        val montoCaja = billetesCaja.entries.sumOf { (denom, cant) ->
            if (cant > 0) cant.toLong() * denom.toLong() else 0L
        }
        val montoCajita = toquitosCajita.entries.sumOf { (denom, cant) ->
            if (cant > 0) cant.toLong() * denom.toLong() * 10L else 0L
        }
        val montoTotal = montoCaja + montoCajita

        require(montoTotal > 0) { "El monto del egreso debe ser mayor a 0" }

        for ((denom, cant) in billetesCaja) {
            if (cant <= 0) continue
            val cActual = conteosActuales[denom]
            val disp = cActual?.billetesCaja ?: 0
            if (disp < cant) {
                throw IllegalStateException("No hay suficientes billetes de $ $denom en caja")
            }
        }

        for ((denom, cant) in toquitosCajita) {
            if (cant <= 0) continue
            val cActual = conteosActuales[denom]
            val disp = cActual?.toquitosCajita ?: 0
            if (disp < cant) {
                throw IllegalStateException("No hay suficientes toquitos de $ $denom en cajita")
            }
        }

        if (tipo == "PROVEEDOR" && !proveedor.isNullOrBlank()) {
            dao.insertProveedor(ProveedorLocal(nombre = proveedor.trim()))
        }

        val todasDenoms = Denominaciones.LISTA.map { it.valor }
        for (denom in todasDenoms) {
            val cantCaja = billetesCaja[denom] ?: 0
            val cantCajita = toquitosCajita[denom] ?: 0
            if (cantCaja > 0 || cantCajita > 0) {
                val cActual = conteosActuales[denom] ?: ConteoLocal(turnoId = turnoId, denominacion = denom, billetesCaja = 0, toquitosCajita = 0)
                val nuevoConteo = cActual.copy(
                    billetesCaja = (cActual.billetesCaja - cantCaja).coerceAtLeast(0),
                    toquitosCajita = (cActual.toquitosCajita - cantCajita).coerceAtLeast(0)
                )
                dao.insertOrUpdateConteo(nuevoConteo)
            }
        }

        val origenReal = when {
            billetesCaja.any { it.value > 0 } && toquitosCajita.any { it.value > 0 } -> "MIXTO"
            toquitosCajita.any { it.value > 0 } -> "CAJITA"
            else -> "CAJA"
        }

        val egresoId = UUID.randomUUID().toString()
        val ahora = System.currentTimeMillis()
        val egresoLocal = EgresoLocal(
            id = UUID.randomUUID().toString(),
            egresoId = egresoId,
            version = 1,
            eliminado = false,
            turnoId = turnoId,
            ocurridoEn = ahora,
            registradoEn = ahora,
            tipo = tipo,
            origen = origenReal,
            monto = montoTotal,
            billetes = VentaLogic.mapEgresoToJsonString(billetesCaja, toquitosCajita),
            proveedor = proveedor?.ifBlank { null },
            nota = nota?.ifBlank { null },
            sincronizado = false
        )

        dao.insertEgreso(egresoLocal)
        triggerSync()
        return egresoLocal
    }

    suspend fun editarEgreso(
        egresoActual: EgresoLocal,
        nuevoTipo: String,
        nuevoOrigen: String,
        nuevosBilletesCaja: Map<Int, Int>,
        nuevosToquitosCajita: Map<Int, Int>,
        nuevoProveedor: String?,
        nuevaNota: String?
    ): EgresoLocal {
        val conteosActuales = dao.getConteos(egresoActual.turnoId).associateBy { it.denominacion }
        val (viejosCaja, viejosCajita) = VentaLogic.parseEgresoJsonString(egresoActual.billetes, egresoActual.origen)

        val montoCaja = nuevosBilletesCaja.entries.sumOf { (denom, cant) ->
            if (cant > 0) cant.toLong() * denom.toLong() else 0L
        }
        val montoCajita = nuevosToquitosCajita.entries.sumOf { (denom, cant) ->
            if (cant > 0) cant.toLong() * denom.toLong() * 10L else 0L
        }
        val nuevoMontoTotal = montoCaja + montoCajita

        require(nuevoMontoTotal > 0) { "El monto del egreso debe ser mayor a 0" }

        val todasDenoms = Denominaciones.LISTA.map { it.valor }
        for (denom in todasDenoms) {
            val cActual = conteosActuales[denom] ?: ConteoLocal(turnoId = egresoActual.turnoId, denominacion = denom, billetesCaja = 0, toquitosCajita = 0)

            val vCaja = viejosCaja[denom] ?: 0
            val vCajita = viejosCajita[denom] ?: 0

            val nCaja = nuevosBilletesCaja[denom] ?: 0
            val nCajita = nuevosToquitosCajita[denom] ?: 0

            val bCaja = cActual.billetesCaja + vCaja - nCaja
            val tCajita = cActual.toquitosCajita + vCajita - nCajita

            if (bCaja < 0 || tCajita < 0) {
                throw IllegalStateException("No hay suficientes billetes/toquitos para aplicar la edición")
            }

            dao.insertOrUpdateConteo(cActual.copy(billetesCaja = bCaja, toquitosCajita = tCajita))
        }

        if (nuevoTipo == "PROVEEDOR" && !nuevoProveedor.isNullOrBlank()) {
            dao.insertProveedor(ProveedorLocal(nombre = nuevoProveedor.trim()))
        }

        val origenReal = when {
            nuevosBilletesCaja.any { it.value > 0 } && nuevosToquitosCajita.any { it.value > 0 } -> "MIXTO"
            nuevosToquitosCajita.any { it.value > 0 } -> "CAJITA"
            else -> "CAJA"
        }

        val nuevaVersion = egresoActual.copy(
            id = UUID.randomUUID().toString(),
            version = egresoActual.version + 1,
            registradoEn = System.currentTimeMillis(),
            tipo = nuevoTipo,
            origen = origenReal,
            monto = nuevoMontoTotal,
            billetes = VentaLogic.mapEgresoToJsonString(nuevosBilletesCaja, nuevosToquitosCajita),
            proveedor = nuevoProveedor?.ifBlank { null },
            nota = nuevaNota?.ifBlank { null },
            sincronizado = false
        )

        dao.insertEgreso(nuevaVersion)
        triggerSync()
        return nuevaVersion
    }

    suspend fun borrarEgreso(egresoActual: EgresoLocal): EgresoLocal {
        val conteosActuales = dao.getConteos(egresoActual.turnoId).associateBy { it.denominacion }
        val (viejosCaja, viejosCajita) = VentaLogic.parseEgresoJsonString(egresoActual.billetes, egresoActual.origen)

        val todasDenoms = Denominaciones.LISTA.map { it.valor }
        for (denom in todasDenoms) {
            val vCaja = viejosCaja[denom] ?: 0
            val vCajita = viejosCajita[denom] ?: 0
            if (vCaja > 0 || vCajita > 0) {
                val cActual = conteosActuales[denom] ?: ConteoLocal(turnoId = egresoActual.turnoId, denominacion = denom, billetesCaja = 0, toquitosCajita = 0)
                val nuevoConteo = cActual.copy(
                    billetesCaja = cActual.billetesCaja + vCaja,
                    toquitosCajita = cActual.toquitosCajita + vCajita
                )
                dao.insertOrUpdateConteo(nuevoConteo)
            }
        }

        val egresoEliminado = egresoActual.copy(
            id = UUID.randomUUID().toString(),
            version = egresoActual.version + 1,
            eliminado = true,
            registradoEn = System.currentTimeMillis(),
            sincronizado = false
        )

        dao.insertEgreso(egresoEliminado)
        triggerSync()
        return egresoEliminado
    }

    suspend fun cerrarTurno(
        turnoId: String,
        posberry: Long,
        nota: String?,
        digitalCargadoJson: String = "{}",
        digitalEditadoJson: String = "{}",
        ventasDigitalesCargadas: Int = 0,
        ventasDigitales: Int = 0,
        totalDigitalCargado: Long = 0,
        totalDigital: Long = 0,
        diferenciaDigital: Long = 0,
        ventasEfectivo: Int = 0,
        totalVentasEfectivo: Long = 0,
        totalRetiros: Long = 0,
        totalProveedores: Long = 0
    ): String {
        val turno = dao.getTurnoById(turnoId) ?: throw IllegalStateException("Turno no encontrado")
        val cierreId = UUID.randomUUID().toString()

        val cierre = CierreLocal(
            id = cierreId,
            turnoId = turnoId,
            cerradoEn = System.currentTimeMillis(),
            totalPosberry = posberry,
            nota = nota?.ifBlank { null },
            sincronizado = false,
            digitalCargado = digitalCargadoJson,
            digitalEditado = digitalEditadoJson,
            ventasDigitalesCargadas = ventasDigitalesCargadas,
            ventasDigitales = ventasDigitales,
            totalDigitalCargado = totalDigitalCargado,
            totalDigital = totalDigital,
            diferenciaDigital = diferenciaDigital,
            ventasEfectivo = ventasEfectivo,
            totalVentasEfectivo = totalVentasEfectivo,
            totalRetiros = totalRetiros,
            totalProveedores = totalProveedores
        )

        dao.insertCierre(cierre)
        dao.updateTurno(turno.copy(estado = "CERRADO"))

        triggerSync()
        return cierreId
    }

    fun triggerSync() {
        SyncWorker.enqueue(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dispositivoId = getOrCreateDispositivoId()
                Sincronizador.ejecutarSincronizacion(context, dao, supabaseService, dispositivoId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
