package com.example.cajita.data.worker

import android.content.Context
import android.util.Log
import com.example.cajita.data.local.dao.CajitaDao
import com.example.cajita.data.remote.CierreSupabaseDto
import com.example.cajita.data.remote.EgresoSupabaseDto
import com.example.cajita.data.remote.MovimientoSupabaseDto
import com.example.cajita.data.remote.SupabaseService
import com.example.cajita.data.remote.TurnoSupabaseDto
import com.example.cajita.data.remote.VentaSupabaseDto
import com.example.cajita.domain.model.VentaLogic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Sincronizador {

    suspend fun ejecutarSincronizacion(
        context: Context,
        dao: CajitaDao,
        supabaseService: SupabaseService,
        dispositivoId: String
    ): Boolean {
        return try {
            // 1. Sync Turnos
            val turnosPendientes = dao.getTurnosPendientesApertura()
            for (turno in turnosPendientes) {
                sincronizarTurno(turno.id, dao, supabaseService, dispositivoId)
            }

            // 2. Sync Movimientos
            val movimientosPendientes = dao.getMovimientosNoSincronizados()
            for (mov in movimientosPendientes) {
                if (!asegurarTurnoSincronizado(mov.turnoId, dao, supabaseService, dispositivoId)) continue

                val dto = MovimientoSupabaseDto(
                    id = mov.id,
                    turnoId = mov.turnoId,
                    ocurridoEn = formatIsoInstant(mov.ocurridoEn),
                    tipo = mov.tipo,
                    denominacion = mov.denominacion,
                    cantidad = mov.cantidad
                )

                if (supabaseService.insertMovimiento(dto)) {
                    dao.marcarMovimientoSincronizado(mov.id)
                }
            }

            // 3. Sync Ventas
            val ventasPendientes = dao.getVentasNoSincronizadas()
            for (venta in ventasPendientes) {
                if (!asegurarTurnoSincronizado(venta.turnoId, dao, supabaseService, dispositivoId)) continue

                val dto = VentaSupabaseDto(
                    id = venta.id,
                    ventaId = venta.ventaId,
                    version = venta.version,
                    turnoId = venta.turnoId,
                    ocurridoEn = formatIsoInstant(venta.ocurridoEn),
                    registradoEn = formatIsoInstant(venta.registradoEn),
                    eliminada = venta.eliminada,
                    medio = venta.medio,
                    totalVenta = venta.totalVenta,
                    montoDigital = venta.montoDigital,
                    efectivoEntrada = venta.efectivoEntrada,
                    efectivoSalida = venta.efectivoSalida,
                    billetesEntrada = stringMapToJsonObject(venta.billetesEntrada),
                    billetesSalida = stringMapToJsonObject(venta.billetesSalida),
                    nota = venta.nota
                )

                if (supabaseService.insertVenta(dto)) {
                    dao.marcarVentaSincronizada(venta.id)
                }
            }

            // 4. Sync Egresos
            val egresosPendientes = dao.getEgresosNoSincronizados()
            for (egreso in egresosPendientes) {
                if (!asegurarTurnoSincronizado(egreso.turnoId, dao, supabaseService, dispositivoId)) continue

                val dto = EgresoSupabaseDto(
                    id = egreso.id,
                    egresoId = egreso.egresoId,
                    version = egreso.version,
                    turnoId = egreso.turnoId,
                    ocurridoEn = formatIsoInstant(egreso.ocurridoEn),
                    registradoEn = formatIsoInstant(egreso.registradoEn),
                    eliminado = egreso.eliminado,
                    tipo = egreso.tipo,
                    origen = egreso.origen,
                    monto = egreso.monto,
                    billetes = stringMapToJsonObject(egreso.billetes),
                    proveedor = egreso.proveedor,
                    nota = egreso.nota
                )

                if (supabaseService.insertEgreso(dto)) {
                    dao.marcarEgresoSincronizado(egreso.id)
                }
            }

            // 5. Sync Cierres
            val cierresPendientes = dao.getCierresNoSincronizados()
            for (cierre in cierresPendientes) {
                if (!asegurarTurnoSincronizado(cierre.turnoId, dao, supabaseService, dispositivoId)) continue

                val conteos = dao.getConteos(cierre.turnoId)
                val billetesMap = mutableMapOf<String, JsonPrimitive>()
                val toquitosMap = mutableMapOf<String, JsonPrimitive>()
                var totalCaja = 0L
                var totalCajita = 0L

                for (c in conteos) {
                    if (c.billetesCaja > 0) {
                        billetesMap[c.denominacion.toString()] = JsonPrimitive(c.billetesCaja)
                        totalCaja += c.billetesCaja.toLong() * c.denominacion.toLong()
                    }
                    if (c.toquitosCajita > 0) {
                        toquitosMap[c.denominacion.toString()] = JsonPrimitive(c.toquitosCajita)
                        totalCajita += c.toquitosCajita.toLong() * 10L * c.denominacion.toLong()
                    }
                }

                val totalContado = totalCaja + totalCajita
                val diferencia = totalContado - cierre.totalPosberry

                val dto = CierreSupabaseDto(
                    id = cierre.id,
                    turnoId = cierre.turnoId,
                    cerradoEn = formatIsoInstant(cierre.cerradoEn),
                    billetesCaja = JsonObject(billetesMap),
                    toquitosCajita = JsonObject(toquitosMap),
                    totalCaja = totalCaja,
                    totalCajita = totalCajita,
                    totalContado = totalContado,
                    totalPosberry = cierre.totalPosberry,
                    diferencia = diferencia,
                    nota = cierre.nota,
                    digitalCargado = stringMapToJsonObjectRaw(cierre.digitalCargado),
                    digitalEditado = stringMapToJsonObjectRaw(cierre.digitalEditado),
                    ventasDigitalesCargadas = cierre.ventasDigitalesCargadas,
                    ventasDigitales = cierre.ventasDigitales,
                    totalDigitalCargado = cierre.totalDigitalCargado,
                    totalDigital = cierre.totalDigital,
                    diferenciaDigital = cierre.diferenciaDigital,
                    ventasEfectivo = cierre.ventasEfectivo,
                    totalVentasEfectivo = cierre.totalVentasEfectivo,
                    totalRetiros = cierre.totalRetiros,
                    totalProveedores = cierre.totalProveedores
                )

                if (supabaseService.insertCierre(dto)) {
                    dao.marcarCierreSincronizado(cierre.id)
                    dao.marcarTurnoCierreSincronizado(cierre.turnoId)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("Sincronizador", "Error en ejecución de sync", e)
            false
        }
    }

    private suspend fun asegurarTurnoSincronizado(
        turnoId: String,
        dao: CajitaDao,
        supabaseService: SupabaseService,
        dispositivoId: String
    ): Boolean {
        val turno = dao.getTurnoById(turnoId) ?: return false
        if (turno.sincronizadoApertura) return true
        return sincronizarTurno(turnoId, dao, supabaseService, dispositivoId)
    }

    private suspend fun sincronizarTurno(
        turnoId: String,
        dao: CajitaDao,
        supabaseService: SupabaseService,
        dispositivoId: String
    ): Boolean {
        val turno = dao.getTurnoById(turnoId) ?: return false
        val conteos = dao.getConteos(turno.id)
        val billetesMap = mutableMapOf<String, JsonPrimitive>()
        val toquitosMap = mutableMapOf<String, JsonPrimitive>()

        for (c in conteos) {
            if (c.billetesCaja > 0) {
                billetesMap[c.denominacion.toString()] = JsonPrimitive(c.billetesCaja)
            }
            if (c.toquitosCajita > 0) {
                toquitosMap[c.denominacion.toString()] = JsonPrimitive(c.toquitosCajita)
            }
        }

        val dto = TurnoSupabaseDto(
            id = turno.id,
            dispositivoId = dispositivoId,
            cajero = turno.cajero,
            abiertoEn = formatIsoInstant(turno.abiertoEn),
            montoAperturaDeclarado = turno.montoAperturaDeclarado,
            billetesApertura = JsonObject(billetesMap),
            toquitosApertura = JsonObject(toquitosMap)
        )

        if (supabaseService.insertTurno(dto)) {
            dao.marcarTurnoAperturaSincronizada(turno.id)
            return true
        }
        return false
    }

    private fun stringMapToJsonObject(jsonStr: String): JsonObject {
        val map = VentaLogic.jsonStringToMap(jsonStr)
        val jsonMap = mutableMapOf<String, JsonPrimitive>()
        for ((denom, cant) in map) {
            if (cant > 0) {
                jsonMap[denom.toString()] = JsonPrimitive(cant)
            }
        }
        return JsonObject(jsonMap)
    }

    private fun stringMapToJsonObjectRaw(jsonStr: String): JsonObject {
        return try {
            if (jsonStr.isBlank()) JsonObject(emptyMap())
            else Json.parseToJsonElement(jsonStr) as? JsonObject ?: JsonObject(emptyMap())
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }
    }

    private fun formatIsoInstant(epochMillis: Long): String {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC))
    }
}
