package com.example.cajita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cajita.data.local.entity.CierreLocal
import com.example.cajita.data.local.entity.ConteoLocal
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.MovimientoLocal
import com.example.cajita.data.local.entity.ProveedorLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.local.entity.VentaLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CajitaDao {

    // --- Turno ---
    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' LIMIT 1")
    fun getTurnoAbiertoFlow(): Flow<TurnoLocal?>

    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' LIMIT 1")
    suspend fun getTurnoAbierto(): TurnoLocal?

    @Query("SELECT * FROM turnos WHERE id = :id LIMIT 1")
    suspend fun getTurnoById(id: String): TurnoLocal?

    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' LIMIT 1")
    suspend fun getTurnoAbiertoDirecto(): TurnoLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoLocal)

    @Update
    suspend fun updateTurno(turno: TurnoLocal)

    @Query("UPDATE turnos SET sincronizadoApertura = 1 WHERE id = :turnoId")
    suspend fun marcarTurnoAperturaSincronizada(turnoId: String)

    @Query("UPDATE turnos SET sincronizadoCierre = 1 WHERE id = :turnoId")
    suspend fun marcarTurnoCierreSincronizado(turnoId: String)

    @Query("SELECT * FROM turnos WHERE sincronizadoApertura = 0")
    suspend fun getTurnosPendientesApertura(): List<TurnoLocal>

    // --- Conteos ---
    @Query("SELECT * FROM conteos WHERE turnoId = :turnoId")
    fun getConteosFlow(turnoId: String): Flow<List<ConteoLocal>>

    @Query("SELECT * FROM conteos WHERE turnoId = :turnoId")
    suspend fun getConteos(turnoId: String): List<ConteoLocal>

    @Query("SELECT * FROM conteos WHERE turnoId = :turnoId AND denominacion = :denominacion LIMIT 1")
    suspend fun getConteoByDenominacion(turnoId: String, denominacion: Int): ConteoLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConteos(conteos: List<ConteoLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConteo(conteo: ConteoLocal)

    // --- Movimientos (Manual count adjustments) ---
    @Query("SELECT * FROM movimientos WHERE turnoId = :turnoId ORDER BY ocurridoEn ASC")
    fun getMovimientosFlow(turnoId: String): Flow<List<MovimientoLocal>>

    @Query("SELECT * FROM movimientos WHERE turnoId = :turnoId ORDER BY ocurridoEn ASC")
    suspend fun getMovimientos(turnoId: String): List<MovimientoLocal>

    @Query("SELECT * FROM movimientos WHERE sincronizado = 0")
    suspend fun getMovimientosNoSincronizados(): List<MovimientoLocal>

    @Query("SELECT * FROM movimientos WHERE turnoId = :turnoId ORDER BY ocurridoEn DESC LIMIT 1")
    suspend fun getUltimoMovimiento(turnoId: String): MovimientoLocal?

    @Query("SELECT COUNT(*) FROM movimientos WHERE turnoId = :turnoId")
    fun getMovimientoCountFlow(turnoId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovimiento(movimiento: MovimientoLocal)

    @Query("DELETE FROM movimientos WHERE id = :id")
    suspend fun deleteMovimiento(id: String)

    @Query("UPDATE movimientos SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarMovimientoSincronizado(id: String)

    // --- Ventas ---
    @Query("""
        SELECT v1.* FROM ventas v1
        INNER JOIN (
            SELECT ventaId, MAX(version) as max_ver
            FROM ventas
            WHERE turnoId = :turnoId
            GROUP BY ventaId
        ) v2 ON v1.ventaId = v2.ventaId AND v1.version = v2.max_ver
        WHERE v1.eliminada = 0
        ORDER BY v1.ocurridoEn DESC
    """)
    fun getVentasActivasFlow(turnoId: String): Flow<List<VentaLocal>>

    @Query("""
        SELECT v1.* FROM ventas v1
        INNER JOIN (
            SELECT ventaId, MAX(version) as max_ver
            FROM ventas
            WHERE turnoId = :turnoId
            GROUP BY ventaId
        ) v2 ON v1.ventaId = v2.ventaId AND v1.version = v2.max_ver
        WHERE v1.eliminada = 0
        ORDER BY v1.ocurridoEn DESC
    """)
    suspend fun getVentasActivas(turnoId: String): List<VentaLocal>

    @Query("""
        SELECT * FROM ventas
        WHERE ventaId = :ventaId
        ORDER BY version DESC
        LIMIT 1
    """)
    suspend fun getUltimaVersionVenta(ventaId: String): VentaLocal?

    @Query("SELECT * FROM ventas WHERE sincronizado = 0")
    suspend fun getVentasNoSincronizadas(): List<VentaLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenta(venta: VentaLocal)

    @Query("UPDATE ventas SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarVentaSincronizada(id: String)

    // --- Egresos ---
    @Query("""
        SELECT e1.* FROM egresos e1
        INNER JOIN (
            SELECT egresoId, MAX(version) as max_ver
            FROM egresos
            WHERE turnoId = :turnoId
            GROUP BY egresoId
        ) e2 ON e1.egresoId = e2.egresoId AND e1.version = e2.max_ver
        WHERE e1.eliminado = 0
        ORDER BY e1.ocurridoEn DESC
    """)
    fun getEgresosActivosFlow(turnoId: String): Flow<List<EgresoLocal>>

    @Query("""
        SELECT e1.* FROM egresos e1
        INNER JOIN (
            SELECT egresoId, MAX(version) as max_ver
            FROM egresos
            WHERE turnoId = :turnoId
            GROUP BY egresoId
        ) e2 ON e1.egresoId = e2.egresoId AND e1.version = e2.max_ver
        WHERE e1.eliminado = 0
        ORDER BY e1.ocurridoEn DESC
    """)
    suspend fun getEgresosActivos(turnoId: String): List<EgresoLocal>

    @Query("""
        SELECT * FROM egresos
        WHERE egresoId = :egresoId
        ORDER BY version DESC
        LIMIT 1
    """)
    suspend fun getUltimaVersionEgreso(egresoId: String): EgresoLocal?

    @Query("SELECT * FROM egresos WHERE sincronizado = 0")
    suspend fun getEgresosNoSincronizados(): List<EgresoLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEgreso(egreso: EgresoLocal)

    @Query("UPDATE egresos SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarEgresoSincronizado(id: String)

    // --- Proveedores ---
    @Query("SELECT * FROM proveedores ORDER BY ultimoUso DESC LIMIT 10")
    fun getProveedoresRecientesFlow(): Flow<List<ProveedorLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProveedor(proveedor: ProveedorLocal)

    // --- Conteo total de operaciones del turno ---
    @Query("""
        SELECT 
          (SELECT COUNT(DISTINCT ventaId) FROM ventas WHERE turnoId = :turnoId AND eliminada = 0) +
          (SELECT COUNT(DISTINCT egresoId) FROM egresos WHERE turnoId = :turnoId AND eliminado = 0)
    """)
    fun getOperacionesCountFlow(turnoId: String): Flow<Int>

    // --- Cierre ---
    @Query("SELECT * FROM cierres ORDER BY cerradoEn DESC LIMIT 1")
    fun getUltimoCierreFlow(): Flow<CierreLocal?>

    @Query("SELECT * FROM cierres ORDER BY cerradoEn DESC LIMIT 1")
    suspend fun getUltimoCierre(): CierreLocal?

    @Query("SELECT * FROM cierres WHERE id = :id LIMIT 1")
    suspend fun getCierreById(id: String): CierreLocal?

    @Query("SELECT * FROM cierres WHERE turnoId = :turnoId LIMIT 1")
    suspend fun getCierreForTurno(turnoId: String): CierreLocal?

    @Query("SELECT * FROM cierres WHERE sincronizado = 0")
    suspend fun getCierresNoSincronizados(): List<CierreLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCierre(cierre: CierreLocal)

    @Query("UPDATE cierres SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarCierreSincronizado(id: String)

    // --- Sync status total ---
    @Query("""
        SELECT 
          (SELECT COUNT(*) FROM turnos WHERE sincronizadoApertura = 0) +
          (SELECT COUNT(*) FROM movimientos WHERE sincronizado = 0) +
          (SELECT COUNT(*) FROM ventas WHERE sincronizado = 0) +
          (SELECT COUNT(*) FROM egresos WHERE sincronizado = 0) +
          (SELECT COUNT(*) FROM cierres WHERE sincronizado = 0)
    """)
    fun getPendingSyncCountFlow(): Flow<Int>
}
