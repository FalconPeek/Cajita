package com.example.cajita.ui.cerrar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.MedioPago
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightWarn

@Composable
fun CerrarCajaPaso1Screen(
    viewModel: CerrarCajaViewModel,
    onNavigateBack: () -> Unit,
    onSiguienteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val warnColor = if (isDark) DarkWarn else LightWarn

    val digitalRows by viewModel.digitalRows.collectAsState()
    val ventasActivas by viewModel.ventasActivas.collectAsState()

    val totalDigitalEditado = digitalRows.values.sumOf { it.montoEditado }
    val totalVentasDigitalesEditadas = digitalRows.values.sumOf { it.ventasEditadas }
    val totalDigitalCargado = digitalRows.values.sumOf { it.montoCargado }
    val totalVentasDigitalesCargadas = digitalRows.values.sumOf { it.ventasCargadas }
    val diferenciaTotal = totalDigitalEditado - totalDigitalCargado

    val ventasEfectivo = ventasActivas.filter { it.medio == MedioPago.EFECTIVO }
    val totalEfectivoCount = ventasEfectivo.size
    val totalEfectivoMonto = ventasEfectivo.sumOf { it.totalVenta }

    var medioAEditar by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // BARRA SUPERIOR Y PROGRESS BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Paso 1 de 2",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ventas digitales",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar 50%
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // HERO TOTAL DIGITAL
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF125C3E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL DIGITAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$totalVentasDigitalesEditadas ventas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = totalDigitalEditado.toFormatoMonedaARS(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Cargado: ${totalDigitalCargado.toFormatoMonedaARS()} · $totalVentasDigitalesCargadas ventas",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Revisá contra el posnet y la app de cobros. Tocá un número para corregirlo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // TABLA INTERACTIVA POR MEDIO
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("MEDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f))
                                Text("VENTAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                Text("SUBTOTAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val mediosOrder = listOf(MedioPago.DEBITO, MedioPago.QR_TRANSFERENCIA, MedioPago.CREDITO)
                            mediosOrder.forEach { medio ->
                                val rowState = digitalRows[medio] ?: DigitalRowState(0, 0L, 0, 0L)
                                val editado = rowState.editadoManualmente

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (editado) warnColor.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { medioAEditar = medio }
                                        .padding(vertical = 8.dp, horizontal = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val (badgeBg, iconChar) = when (medio) {
                                            MedioPago.DEBITO -> Color(0xFF2B80FF) to "▰"
                                            MedioPago.QR_TRANSFERENCIA -> Color(0xFF9C42F5) to "▦"
                                            else -> Color(0xFFE942A5) to "◆"
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(badgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(iconChar, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = MedioPago.toLabel(medio),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (editado) warnColor else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1.3f)
                                        )

                                        Text(
                                            text = rowState.ventasEditadas.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                            color = if (editado) warnColor else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = rowState.montoEditado.toFormatoMonedaARS(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.End,
                                            color = if (editado) warnColor else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1.5f)
                                        )
                                    }

                                    if (editado) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val diffV = rowState.ventasEditadas - rowState.ventasCargadas
                                        val diffM = rowState.montoEditado - rowState.montoCargado
                                        val signoV = if (diffV >= 0) "+$diffV" else "$diffV"
                                        val signoM = if (diffM >= 0) "+${diffM.toFormatoMonedaARS()}" else "- ${Math.abs(diffM).toFormatoMonedaARS()}"

                                        Text(
                                            text = "antes ${rowState.ventasCargadas} · ${rowState.montoCargado.toFormatoMonedaARS()}   $signoV venta · $signoM",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = warnColor,
                                            modifier = Modifier.padding(start = 38.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TARJETA DIFERENCIA CON LO CARGADO
                item {
                    val (cardBg, cardFg, tituloDiff) = when {
                        diferenciaTotal != 0L -> Triple(warnColor.copy(alpha = 0.15f), warnColor, if (diferenciaTotal > 0) "+ ${diferenciaTotal.toFormatoMonedaARS()}" else "- ${Math.abs(diferenciaTotal).toFormatoMonedaARS()}")
                        else -> Triple(Color(0xFF125C3E).copy(alpha = 0.15f), Color(0xFF125C3E), "✓ Cuadra justo")
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Diferencia con lo cargado",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = cardFg
                                )
                                Text(
                                    text = tituloDiff,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = cardFg
                                )
                            }

                            if (diferenciaTotal != 0L) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Modificado manualmente durante el cierre",
                                    fontSize = 12.sp,
                                    color = cardFg
                                )
                            }
                        }
                    }
                }

                // TARJETA INFORMATIVA EFECTIVO
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF125C3E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Efectivo (informativo)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$totalEfectivoCount · ${totalEfectivoMonto.toFormatoMonedaARS()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // BOTÓN SIGUIENTE
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSiguienteClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Siguiente: arqueo de efectivo →",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // DIÁLOGO PARA EDITAR FILA DIGITAL
    if (medioAEditar != null) {
        val medioKey = medioAEditar!!
        val rowState = digitalRows[medioKey] ?: DigitalRowState(0, 0L, 0, 0L)

        var ventasTemp by remember { mutableStateOf(rowState.ventasEditadas.toString()) }
        var montoTemp by remember { mutableStateOf(rowState.montoEditado.toString()) }

        AlertDialog(
            onDismissRequest = { medioAEditar = null },
            title = { Text("Editar ${MedioPago.toLabel(medioKey)}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ventasTemp,
                        onValueChange = { ventasTemp = it.filter { c -> c.isDigit() } },
                        label = { Text("Cantidad de ventas") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = montoTemp,
                        onValueChange = { montoTemp = it.filter { c -> c.isDigit() } },
                        label = { Text("Subtotal ($)") },
                        prefix = { Text("$ ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val v = ventasTemp.toIntOrNull() ?: 0
                        val m = montoTemp.toLongOrNull() ?: 0L
                        viewModel.editarFilaDigital(medioKey, v, m)
                        medioAEditar = null
                    }
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { medioAEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
