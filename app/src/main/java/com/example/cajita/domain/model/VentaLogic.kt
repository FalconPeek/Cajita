package com.example.cajita.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object MedioPago {
    const val EFECTIVO = "efectivo"
    const val DEBITO = "debito"
    const val QR_TRANSFERENCIA = "qr_transferencia"
    const val CREDITO = "credito"

    fun toLabel(medio: String): String = when (medio) {
        EFECTIVO -> "Efectivo"
        DEBITO -> "Débito"
        QR_TRANSFERENCIA -> "QR / Transf."
        CREDITO -> "Crédito"
        else -> medio
    }
}

object VentaLogic {

    fun calcularMontoBilletes(mapaBilletes: Map<Int, Int>): Long {
        return mapaBilletes.entries.sumOf { (denom, cant) ->
            if (cant > 0) cant.toLong() * denom.toLong() else 0L
        }
    }

    fun esVentaValida(
        montoDigital: Long,
        efectivoEntrada: Long,
        efectivoSalida: Long,
        totalVenta: Long
    ): Boolean {
        if (totalVenta <= 0) return false
        return (montoDigital + efectivoEntrada - efectivoSalida) == totalVenta
    }

    /**
     * Algoritmo de sugerir vuelto de mayor a menor denominación.
     * Considera los billetes disponibles en la caja MÁS los billetes ingresados en la misma venta (`billetesEntrada`).
     */
    fun sugerirVuelto(
        vueltoRequerido: Long,
        cajaBilletes: Map<Int, Int>,
        billetesEntrada: Map<Int, Int> = emptyMap()
    ): Map<Int, Int>? {
        if (vueltoRequerido <= 0) return emptyMap()

        // Billetes disponibles combinados
        val disponibles = mutableMapOf<Int, Int>()
        Denominaciones.LISTA.forEach { item ->
            val enCaja = cajaBilletes[item.valor] ?: 0
            val entraron = billetesEntrada[item.valor] ?: 0
            disponibles[item.valor] = (enCaja + entraron).coerceAtLeast(0)
        }

        var restante = vueltoRequerido
        val resultado = mutableMapOf<Int, Int>()

        // Denominaciones ordenadas de mayor a menor
        val denoms = Denominaciones.LISTA.map { it.valor }.sortedDescending()

        for (denom in denoms) {
            if (restante <= 0) break
            val disp = disponibles[denom] ?: 0
            if (disp <= 0 || denom > restante) continue

            val necesarios = (restante / denom).toInt()
            val aUsar = minOf(necesarios, disp)

            if (aUsar > 0) {
                resultado[denom] = aUsar
                restante -= aUsar.toLong() * denom.toLong()
            }
        }

        return if (restante == 0L) resultado else null
    }

    fun mapToJsonString(mapa: Map<Int, Int>): String {
        val jsonMap = mapa.filter { it.value > 0 }.mapKeys { it.key.toString() }
        val jsonObjectMap = jsonMap.mapValues { JsonPrimitive(it.value) }
        return JsonObject(jsonObjectMap).toString()
    }

    fun jsonStringToMap(jsonStr: String): Map<Int, Int> {
        if (jsonStr.isBlank() || jsonStr == "{}") return emptyMap()
        return try {
            val jsonObject = Json.parseToJsonElement(jsonStr) as? JsonObject ?: return emptyMap()
            val result = mutableMapOf<Int, Int>()
            jsonObject.forEach { (key, value) ->
                val denom = key.toIntOrNull()
                val cant = value.jsonPrimitive.intOrNull
                if (denom != null && cant != null && cant > 0) {
                    result[denom] = cant
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
