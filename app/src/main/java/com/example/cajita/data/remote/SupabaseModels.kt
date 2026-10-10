package com.example.cajita.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TurnoSupabaseDto(
    val id: String,
    @SerialName("dispositivo_id")
    val dispositivoId: String,
    val cajero: String?,
    @SerialName("abierto_en")
    val abiertoEn: String,
    @SerialName("monto_apertura_declarado")
    val montoAperturaDeclarado: Long,
    @SerialName("billetes_apertura")
    val billetesApertura: JsonObject,
    @SerialName("toquitos_apertura")
    val toquitosApertura: JsonObject
)

@Serializable
data class MovimientoSupabaseDto(
    val id: String,
    @SerialName("turno_id")
    val turnoId: String,
    @SerialName("ocurrido_en")
    val ocurridoEn: String,
    val tipo: String,
    val denominacion: Int,
    val cantidad: Int
)

@Serializable
data class VentaSupabaseDto(
    val id: String,
    @SerialName("venta_id")
    val ventaId: String,
    val version: Int,
    @SerialName("turno_id")
    val turnoId: String,
    @SerialName("ocurrido_en")
    val ocurridoEn: String,
    @SerialName("registrado_en")
    val registradoEn: String,
    val eliminada: Boolean,
    val medio: String,
    @SerialName("total_venta")
    val totalVenta: Long,
    @SerialName("monto_digital")
    val montoDigital: Long,
    @SerialName("efectivo_entrada")
    val efectivoEntrada: Long,
    @SerialName("efectivo_salida")
    val efectivoSalida: Long,
    @SerialName("billetes_entrada")
    val billetesEntrada: JsonObject,
    @SerialName("billetes_salida")
    val billetesSalida: JsonObject,
    val nota: String? = null
)

@Serializable
data class EgresoSupabaseDto(
    val id: String,
    @SerialName("egreso_id")
    val egresoId: String,
    val version: Int,
    @SerialName("turno_id")
    val turnoId: String,
    @SerialName("ocurrido_en")
    val ocurridoEn: String,
    @SerialName("registrado_en")
    val registradoEn: String,
    val eliminado: Boolean,
    val tipo: String,
    val origen: String,
    val monto: Long,
    val billetes: JsonObject,
    val proveedor: String? = null,
    val nota: String? = null
)

@Serializable
data class CierreSupabaseDto(
    val id: String,
    @SerialName("turno_id")
    val turnoId: String,
    @SerialName("cerrado_en")
    val cerradoEn: String,
    @SerialName("billetes_caja")
    val billetesCaja: JsonObject,
    @SerialName("toquitos_cajita")
    val toquitosCajita: JsonObject,
    @SerialName("total_caja")
    val totalCaja: Long,
    @SerialName("total_cajita")
    val totalCajita: Long,
    @SerialName("total_contado")
    val totalContado: Long,
    @SerialName("total_posberry")
    val totalPosberry: Long,
    val diferencia: Long,
    val nota: String? = null,
    @SerialName("digital_cargado")
    val digitalCargado: JsonObject? = null,
    @SerialName("digital_editado")
    val digitalEditado: JsonObject? = null,
    @SerialName("ventas_digitales_cargadas")
    val ventasDigitalesCargadas: Int = 0,
    @SerialName("ventas_digitales")
    val ventasDigitales: Int = 0,
    @SerialName("total_digital_cargado")
    val totalDigitalCargado: Long = 0,
    @SerialName("total_digital")
    val totalDigital: Long = 0,
    @SerialName("diferencia_digital")
    val diferenciaDigital: Long = 0,
    @SerialName("ventas_efectivo")
    val ventasEfectivo: Int = 0,
    @SerialName("total_ventas_efectivo")
    val totalVentasEfectivo: Long = 0,
    @SerialName("total_retiros")
    val totalRetiros: Long = 0,
    @SerialName("total_proveedores")
    val totalProveedores: Long = 0
)
