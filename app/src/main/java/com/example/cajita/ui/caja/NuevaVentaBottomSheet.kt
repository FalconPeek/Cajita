package com.example.cajita.ui.caja

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.Denominaciones
import com.example.cajita.domain.model.MedioPago
import com.example.cajita.domain.model.VentaLogic
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.components.DenominationChip
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightError
import com.example.cajita.ui.theme.LightOk
import com.example.cajita.ui.theme.LightWarn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NuevaVentaBottomSheet(
    cajaBilletes: Map<Int, Int>,
    ventaAEditar: com.example.cajita.data.local.entity.VentaLocal? = null,
    onDismiss: () -> Unit,
    onGuardarVenta: (
        medio: String,
        totalVenta: Long,
        montoDigital: Long,
        billetesEntrada: Map<Int, Int>,
        billetesSalida: Map<Int, Int>,
        nota: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDark = isSystemInDarkTheme()
    val okColor = if (isDark) DarkOk else LightOk
    val warnColor = if (isDark) DarkWarn else LightWarn
    val errorColor = if (isDark) DarkError else LightError

    var totalTexto by remember {
        mutableStateOf(ventaAEditar?.totalVenta?.toString() ?: "")
    }

    var medioSeleccionado by remember {
        mutableStateOf(ventaAEditar?.medio ?: MedioPago.EFECTIVO)
    }

    var montoDigitalTexto by remember {
        mutableStateOf(ventaAEditar?.montoDigital?.toString() ?: "")
    }

    val billetesEntrada = remember {
        mutableStateMapOf<Int, Int>().apply {
            if (ventaAEditar != null) {
                putAll(VentaLogic.jsonStringToMap(ventaAEditar.billetesEntrada))
            }
        }
    }

    val billetesSalida = remember {
        mutableStateMapOf<Int, Int>().apply {
            if (ventaAEditar != null) {
                putAll(VentaLogic.jsonStringToMap(ventaAEditar.billetesSalida))
            }
        }
    }

    var editandoMontoDigitalManualmente by remember { mutableStateOf(ventaAEditar != null) }

    val totalVentaLong = totalTexto.toLongOrNull() ?: 0L

    // Si no editó digital manualmente y cambia el total, sincronizar montoDigital
    LaunchedEffect(totalTexto, medioSeleccionado) {
        if (medioSeleccionado != MedioPago.EFECTIVO && !editandoMontoDigitalManualmente) {
            montoDigitalTexto = totalTexto
        }
    }

    val montoDigitalLong = if (medioSeleccionado == MedioPago.EFECTIVO) 0L else (montoDigitalTexto.toLongOrNull() ?: 0L)
    val efectivoEntradaLong = VentaLogic.calcularMontoBilletes(billetesEntrada)
    val efectivoSalidaLong = VentaLogic.calcularMontoBilletes(billetesSalida)

    val esDigital = medioSeleccionado != MedioPago.EFECTIVO
    val esExtraccion = esDigital && montoDigitalLong > totalVentaLong && totalVentaLong > 0
    val esPagoMixto = esDigital && montoDigitalLong < totalVentaLong && montoDigitalLong > 0
    val diferenciaExtraccion = if (esExtraccion) montoDigitalLong - totalVentaLong else 0L
    val diferenciaMixto = if (esPagoMixto) totalVentaLong - montoDigitalLong else 0L

    val ventaCuadra = VentaLogic.esVentaValida(
        montoDigital = montoDigitalLong,
        efectivoEntrada = efectivoEntradaLong,
        efectivoSalida = efectivoSalidaLong,
        totalVenta = totalVentaLong
    )

    val horaActualStr = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val fecha = if (ventaAEditar != null) Date(ventaAEditar.ocurridoEn) else Date()
        sdf.format(fecha)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (ventaAEditar == null) "Nueva venta" else "Editar venta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = horaActualStr,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "TOTAL DE LA COMPRA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = totalTexto,
                            onValueChange = { nuevo ->
                                totalTexto = nuevo.filter { it.isDigit() }
                            },
                            prefix = {
                                Text("$ ", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            },
                            placeholder = { Text("0", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = "¿CÓMO PAGÓ?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val mediosList = listOf(
                                MedioPago.EFECTIVO to "Efectivo",
                                MedioPago.DEBITO to "Débito",
                                MedioPago.QR_TRANSFERENCIA to "QR / Transf.",
                                MedioPago.CREDITO to "Crédito"
                            )

                            mediosList.forEach { (medioKey, label) ->
                                val seleccionado = medioSeleccionado == medioKey
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (seleccionado) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (seleccionado) 2.dp else 1.dp,
                                        color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clickable {
                                            medioSeleccionado = medioKey
                                            if (medioKey != MedioPago.EFECTIVO && !editandoMontoDigitalManualmente) {
                                                montoDigitalTexto = totalTexto
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = when (medioKey) {
                                                MedioPago.EFECTIVO -> "$"
                                                MedioPago.DEBITO -> "▰"
                                                MedioPago.QR_TRANSFERENCIA -> "▦"
                                                else -> "◆"
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (esDigital) {
                    item {
                        Column {
                            OutlinedTextField(
                                value = montoDigitalTexto,
                                onValueChange = { nuevo ->
                                    montoDigitalTexto = nuevo.filter { it.isDigit() }
                                    editandoMontoDigitalManualmente = true
                                },
                                label = { Text("Cobrado por ${MedioPago.toLabel(medioSeleccionado)}") },
                                prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                            )

                            if (esExtraccion) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cobraste ${diferenciaExtraccion.toFormatoMonedaARS()} de más: es extracción, dale el efectivo.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = warnColor
                                )
                            }
                        }
                    }

                    if (esExtraccion || esPagoMixto) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val pestanaSalio = esExtraccion
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (!pestanaSalio) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (esPagoMixto) "Entró · ${diferenciaMixto.toFormatoMonedaARS()}" else "Entró a la caja",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (pestanaSalio) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (esExtraccion) "Salió · ${diferenciaExtraccion.toFormatoMonedaARS()}" else "Salió de la caja",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Grilla de 8 denominaciones en 2 filas de 4
                                CustomDenominationGrid(
                                    mapaBilletes = if (esExtraccion) billetesSalida else billetesEntrada,
                                    signoMas = !esExtraccion,
                                    onIncrement = { denom ->
                                        val mapaTarget = if (esExtraccion) billetesSalida else billetesEntrada
                                        mapaTarget[denom] = (mapaTarget[denom] ?: 0) + 1
                                    },
                                    onDecrement = { denom ->
                                        val mapaTarget = if (esExtraccion) billetesSalida else billetesEntrada
                                        val cant = (mapaTarget[denom] ?: 0) - 1
                                        if (cant <= 0) mapaTarget.remove(denom) else mapaTarget[denom] = cant
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // MEDIO EFECTIVO
                    item {
                        Column {
                            Text(
                                text = "TE DIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            CustomDenominationGrid(
                                mapaBilletes = billetesEntrada,
                                signoMas = true,
                                onIncrement = { denom ->
                                    billetesEntrada[denom] = (billetesEntrada[denom] ?: 0) + 1
                                },
                                onDecrement = { denom ->
                                    val cant = (billetesEntrada[denom] ?: 0) - 1
                                    if (cant <= 0) billetesEntrada.remove(denom) else billetesEntrada[denom] = cant
                                }
                            )
                        }
                    }

                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LE DISTE DE VUELTO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val vueltoRequerido = efectivoEntradaLong - totalVentaLong
                                if (vueltoRequerido > 0) {
                                    Text(
                                        text = "SUGERIR VUELTO",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = okColor,
                                        modifier = Modifier
                                            .clickable {
                                                val sugerido = VentaLogic.sugerirVuelto(
                                                    vueltoRequerido = vueltoRequerido,
                                                    cajaBilletes = cajaBilletes,
                                                    billetesEntrada = billetesEntrada
                                                )
                                                if (sugerido != null) {
                                                    billetesSalida.clear()
                                                    billetesSalida.putAll(sugerido)
                                                }
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            CustomDenominationGrid(
                                mapaBilletes = billetesSalida,
                                signoMas = false,
                                onIncrement = { denom ->
                                    billetesSalida[denom] = (billetesSalida[denom] ?: 0) + 1
                                },
                                onDecrement = { denom ->
                                    val cant = (billetesSalida[denom] ?: 0) - 1
                                    if (cant <= 0) billetesSalida.remove(denom) else billetesSalida[denom] = cant
                                }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // BARRA DE BALANCE Y BOTÓN DE GUARDAR
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (ventaCuadra) okColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (ventaCuadra) "✓ " else "▲ ",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (ventaCuadra) okColor else errorColor,
                        fontSize = 16.sp
                    )

                    val textoBalance = when {
                        ventaCuadra -> {
                            if (esDigital) {
                                if (esExtraccion) {
                                    "${montoDigitalLong.toFormatoMonedaARS()} ${MedioPago.toLabel(medioSeleccionado)} − ${efectivoSalidaLong.toFormatoMonedaARS()} salió = ${totalVentaLong.toFormatoMonedaARS()}"
                                } else if (esPagoMixto) {
                                    "${montoDigitalLong.toFormatoMonedaARS()} ${MedioPago.toLabel(medioSeleccionado)} + ${efectivoEntradaLong.toFormatoMonedaARS()} entró = ${totalVentaLong.toFormatoMonedaARS()}"
                                } else {
                                    "${montoDigitalLong.toFormatoMonedaARS()} ${MedioPago.toLabel(medioSeleccionado)} = ${totalVentaLong.toFormatoMonedaARS()}"
                                }
                            } else {
                                "${efectivoEntradaLong.toFormatoMonedaARS()} entró − ${efectivoSalidaLong.toFormatoMonedaARS()} vuelto = ${totalVentaLong.toFormatoMonedaARS()}"
                            }
                        }
                        else -> {
                            val diff = totalVentaLong - (montoDigitalLong + efectivoEntradaLong - efectivoSalidaLong)
                            if (diff > 0) "Faltan ${diff.toFormatoMonedaARS()} para completar la compra"
                            else "Sobran ${Math.abs(diff).toFormatoMonedaARS()} en el pago"
                        }
                    }

                    Text(
                        text = textoBalance,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ventaCuadra) okColor else errorColor
                    )
                }
            }

            Button(
                onClick = {
                    if (ventaCuadra && totalVentaLong > 0) {
                        onGuardarVenta(
                            medioSeleccionado,
                            totalVentaLong,
                            montoDigitalLong,
                            billetesEntrada.toMap(),
                            billetesSalida.toMap(),
                            null
                        )
                        onDismiss()
                    }
                },
                enabled = ventaCuadra && totalVentaLong > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (ventaAEditar == null) "Guardar venta" else "Guardar cambios",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomDenominationGrid(
    mapaBilletes: Map<Int, Int>,
    signoMas: Boolean,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit
) {
    val denoms = Denominaciones.LISTA.take(8).map { it.valor }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val filas = denoms.chunked(4)
        for (fila in filas) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (denom in fila) {
                    val cantidad = mapaBilletes[denom] ?: 0
                    val tieneValor = cantidad > 0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (tieneValor) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (tieneValor) 1.5.dp else 1.dp,
                            color = if (tieneValor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .combinedClickable(
                                onClick = { onIncrement(denom) },
                                onLongClick = { onDecrement(denom) }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DenominationChip(denominacion = denom, modifier = Modifier.scale(0.85f))

                            val textoCant = when {
                                cantidad > 0 -> if (signoMas) "+$cantidad" else "-$cantidad"
                                else -> "·"
                            }

                            Text(
                                text = textoCant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (tieneValor) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size(width = (46 * scale).dp, height = (30 * scale).dp)
)
