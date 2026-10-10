package com.example.cajita.ui.cerrar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.MedioPago
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightError
import com.example.cajita.ui.theme.LightOk
import com.example.cajita.ui.theme.LightWarn

@Composable
fun ConfirmarCierreScreen(
    viewModel: CerrarCajaViewModel,
    onNavigateBack: () -> Unit,
    onCierreConfirmado: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val okColor = if (isDark) DarkOk else LightOk
    val warnColor = if (isDark) DarkWarn else LightWarn
    val errorColor = if (isDark) DarkError else LightError

    val digitalRows by viewModel.digitalRows.collectAsState()
    val conteos by viewModel.conteos.collectAsState()
    val egresosActivos by viewModel.egresosActivos.collectAsState()
    val posberryLong = viewModel.calcularPosberryLong()
    val nota by viewModel.nota.collectAsState()

    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    val totalDigitalEditado = digitalRows.values.sumOf { it.montoEditado }
    val totalVentasDigitalesEditadas = digitalRows.values.sumOf { it.ventasEditadas }
    val totalDigitalCargado = digitalRows.values.sumOf { it.montoCargado }
    val diferenciaDigitalTotal = totalDigitalEditado - totalDigitalCargado

    val debitoRow = digitalRows[MedioPago.DEBITO] ?: DigitalRowState(0, 0L, 0, 0L)
    val qrRow = digitalRows[MedioPago.QR_TRANSFERENCIA] ?: DigitalRowState(0, 0L, 0, 0L)
    val creditoRow = digitalRows[MedioPago.CREDITO] ?: DigitalRowState(0, 0L, 0, 0L)

    val totalContado = viewModel.calcularTotalContado(conteos)
    val totalSalidasEgresos = egresosActivos.sumOf { it.monto }
    val diferenciaEfectivo = totalContado - posberryLong

    val totalGeneralCierre = totalDigitalEditado + totalContado

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // BARRA SUPERIOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirmar cierre",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // HERO CARD TOTAL CIERRE (TOTAL GENERAL = EFECTIVO SUBTOTAL + DIGITAL SUBTOTAL)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF125C3E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "TOTAL GENERAL TURNO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = totalGeneralCierre.toFormatoMonedaARS(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Subtotal Efectivo", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                        Text(totalContado.toFormatoMonedaARS(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Subtotal Digital", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                        Text(totalDigitalEditado.toFormatoMonedaARS(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // CARD DIGITAL
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "DIGITAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(totalDigitalEditado.toFormatoMonedaARS(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ventas", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalVentasDigitalesEditadas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Débito", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${debitoRow.ventasEditadas} · ${debitoRow.montoEditado.toFormatoMonedaARS()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("QR / Transferencia", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${qrRow.ventasEditadas} · ${qrRow.montoEditado.toFormatoMonedaARS()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Crédito", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${creditoRow.ventasEditadas} · ${creditoRow.montoEditado.toFormatoMonedaARS()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val diffDigText = when {
                                diferenciaDigitalTotal > 0 -> "+ ${diferenciaDigitalTotal.toFormatoMonedaARS()}"
                                diferenciaDigitalTotal < 0 -> "- ${Math.abs(diferenciaDigitalTotal).toFormatoMonedaARS()}"
                                else -> "✓ $ 0"
                            }
                            val diffDigColor = if (diferenciaDigitalTotal == 0L) okColor else warnColor

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Diferencia con lo cargado", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(diffDigText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = diffDigColor)
                            }
                        }
                    }
                }

                // CARD EFECTIVO
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "EFECTIVO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contado", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(totalContado.toFormatoMonedaARS(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("POSBerry", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(posberryLong.toFormatoMonedaARS(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Retiros y proveedores", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("- ${totalSalidasEgresos.toFormatoMonedaARS()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = errorColor)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val diffEfectText = when {
                                diferenciaEfectivo == 0L -> "✓ $ 0"
                                diferenciaEfectivo > 0 -> "▲ + ${diferenciaEfectivo.toFormatoMonedaARS()}"
                                else -> "▲ - ${Math.abs(diferenciaEfectivo).toFormatoMonedaARS()}"
                            }
                            val diffEfectColor = when {
                                diferenciaEfectivo == 0L -> okColor
                                diferenciaEfectivo > 0 -> warnColor
                                else -> errorColor
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Diferencia", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(diffEfectText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = diffEfectColor)
                            }
                        }
                    }
                }

                // NOTA OPCIONAL
                item {
                    OutlinedTextField(
                        value = nota,
                        onValueChange = { viewModel.onNotaChanged(it) },
                        placeholder = { Text("Nota (opcional) · ej. faltó cargar 1 QR") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // BOTÓN CONFIRMAR CIERRE
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { mostrarDialogoConfirmacion = true },
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
                        text = "Confirmar cierre",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Confirmar cierre de caja", fontWeight = FontWeight.Bold) },
            text = { Text("Se guardará el cierre localmente y se enviará cuando haya conexión.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        viewModel.confirmarCierre(onCierreConfirmado)
                    }
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
