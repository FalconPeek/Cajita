package com.example.cajita.domain.model

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

data class DenominacionItem(
    val valor: Int,
    val etiquetaChip: String,
    val colorHex: Color
)

object Denominaciones {
    val LISTA = listOf(
        DenominacionItem(20000, "20K", Color(0xFF5A4FB0)),
        DenominacionItem(10000, "10K", Color(0xFF2F6FB3)),
        DenominacionItem(2000, "2K", Color(0xFFC2507A)),
        DenominacionItem(1000, "1K", Color(0xFFD9722A)),
        DenominacionItem(500, "500", Color(0xFF6FA23A)),
        DenominacionItem(200, "200", Color(0xFF2E9165)),
        DenominacionItem(100, "100", Color(0xFF8458A6)),
        DenominacionItem(50, "50", Color(0xFF5E8AA6)),
        DenominacionItem(20, "20", Color(0xFFB8433F)),
        DenominacionItem(10, "10", Color(0xFFB07A2E))
    )

    fun getItem(valor: Int): DenominacionItem {
        return LISTA.find { it.valor == valor }
            ?: DenominacionItem(valor, valor.toString(), Color(0xFF125C3E))
    }

    const val BILLETES_POR_TOQUITO = 10
}

fun Long.toFormatoMonedaARS(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    val formattedNumber = formatter.format(this)
    return "$ $formattedNumber"
}

fun Int.toFormatoMonedaARS(): String {
    return this.toLong().toFormatoMonedaARS()
}
