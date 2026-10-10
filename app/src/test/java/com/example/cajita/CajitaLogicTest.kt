package com.example.cajita

import com.example.cajita.domain.model.Denominaciones
import com.example.cajita.domain.model.VentaLogic
import com.example.cajita.domain.model.toFormatoMonedaARS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CajitaLogicTest {

    @Test
    fun testCalculoTotalesCajaYCajita() {
        val billetes20k = 3
        val billetes10k = 12
        val toquitos10k = 2

        val totalCaja = (billetes20k * 20000) + (billetes10k * 10000)
        val totalCajita = toquitos10k * 10 * 10000
        val totalGeneral = totalCaja + totalCajita

        assertEquals(180000L, totalCaja.toLong())
        assertEquals(200000L, totalCajita.toLong())
        assertEquals(380000L, totalGeneral.toLong())
    }

    @Test
    fun testBalanceVenta4Casos() {
        // Caso 1: Efectivo con vuelto
        // Entrada $20.000, salida $6.600, total $13.400
        val e1 = 20000L
        val s1 = 6600L
        val t1 = 13400L
        assertTrue(VentaLogic.esVentaValida(montoDigital = 0L, efectivoEntrada = e1, efectivoSalida = s1, totalVenta = t1))

        // Caso 2: Digital exacto
        // QR $8.000, total $8.000
        val d2 = 8000L
        val t2 = 8000L
        assertTrue(VentaLogic.esVentaValida(montoDigital = d2, efectivoEntrada = 0L, efectivoSalida = 0L, totalVenta = t2))

        // Caso 3: Digital con extracción
        // QR $20.000, salió efectivo $12.000, total compra $8.000
        val d3 = 20000L
        val s3 = 12000L
        val t3 = 8000L
        assertTrue(VentaLogic.esVentaValida(montoDigital = d3, efectivoEntrada = 0L, efectivoSalida = s3, totalVenta = t3))

        // Caso 4: Pago mixto
        // QR $10.000, entró efectivo $5.000, total compra $15.000
        val d4 = 10000L
        val e4 = 5000L
        val t4 = 15000L
        assertTrue(VentaLogic.esVentaValida(montoDigital = d4, efectivoEntrada = e4, efectivoSalida = 0L, totalVenta = t4))

        // Caso Inválido: no cuadra
        assertFalse(VentaLogic.esVentaValida(montoDigital = 10000L, efectivoEntrada = 2000L, efectivoSalida = 0L, totalVenta = 15000L))
    }

    @Test
    fun testSugerirVuelto() {
        val caja = mapOf(
            2000 to 10,
            500 to 5,
            200 to 5,
            100 to 10
        )

        val vueltoSugerido = VentaLogic.sugerirVuelto(vueltoRequerido = 6600L, cajaBilletes = caja)
        assertNotNull(vueltoSugerido)
        assertEquals(3, vueltoSugerido!![2000]) // 3 x $2000 = $6000
        assertEquals(1, vueltoSugerido[500])  // 1 x $500 = $500
        assertEquals(1, vueltoSugerido[100])  // 1 x $100 = $100
        assertEquals(6600L, VentaLogic.calcularMontoBilletes(vueltoSugerido))
    }

    @Test
    fun testConversionMapToJsonString() {
        val billetes = mapOf(20000 to 1, 10000 to 0, 2000 to 3)
        val jsonStr = VentaLogic.mapToJsonString(billetes)
        assertTrue(jsonStr.contains("20000"))
        assertTrue(jsonStr.contains("2000"))
        assertFalse(jsonStr.contains("10000")) // no incluye ceros

        val mapaParseado = VentaLogic.jsonStringToMap(jsonStr)
        assertEquals(1, mapaParseado[20000])
        assertEquals(3, mapaParseado[2000])
    }

    @Test
    fun testConversionToquitos() {
        val billetes10k = 23
        val toquitosPaso1 = billetes10k / Denominaciones.BILLETES_POR_TOQUITO
        val restoBilletes = billetes10k % Denominaciones.BILLETES_POR_TOQUITO

        assertEquals(2, toquitosPaso1)
        assertEquals(3, restoBilletes)
    }

    @Test
    fun testCalculoDiferencia() {
        val posberry = 555000L
        val contadoCuadra = 555000L
        val contadoSobra = 560000L
        val contadoFalta = 553000L

        assertEquals(0L, contadoCuadra - posberry)
        assertEquals(5000L, contadoSobra - posberry)
        assertEquals(-2000L, contadoFalta - posberry)
    }

    @Test
    fun testLimiteNoNegativos() {
        val billetes = 2
        val deltaRestar = -5
        val resultado = (billetes + deltaRestar).coerceAtLeast(0)

        assertEquals(0, resultado)
        assertTrue(resultado >= 0)
    }

    @Test
    fun testFormatoMonedaARS() {
        val monto = 553000L
        val formateado = monto.toFormatoMonedaARS()
        assertTrue(formateado.contains("553"))
        assertTrue(formateado.contains("$"))
    }
}
