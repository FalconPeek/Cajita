package com.example.cajita.ui.caja

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.data.local.entity.ProveedorLocal
import com.example.cajita.domain.model.Denominaciones
import com.example.cajita.domain.model.VentaLogic
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.components.DenominationChip
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.LightError

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SacarPlataBottomSheet(
    cajaBilletes: Map<Int, Int>,
    cajitaToquitos: Map<Int, Int>,
    proveedoresRecientes: List<ProveedorLocal> = emptyList(),
    egresoAEditar: com.example.cajita.data.local.entity.EgresoLocal? = null,
    onDismiss: () -> Unit,
    onRegistrarEgreso: (
        tipo: String,
        origen: String,
        billetesCaja: Map<Int, Int>,
        toquitosCajita: Map<Int, Int>,
        proveedor: String?,
        nota: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDark = isSystemInDarkTheme()
    val errorColor = if (isDark) DarkError else LightError

    var tipoSeleccionado by remember {
        mutableStateOf(egresoAEditar?.tipo ?: "PROVEEDOR")
    }

    var tabActivo by remember {
        mutableStateOf(if (egresoAEditar?.origen == "CAJITA") "CAJITA" else "CAJA")
    }

    var proveedorTexto by remember {
        mutableStateOf(egresoAEditar?.proveedor ?: "")
    }

    var notaTexto by remember {
        mutableStateOf(egresoAEditar?.nota ?: "")
    }

    val (cajaInicial, cajitaInicial) = remember(egresoAEditar) {
        if (egresoAEditar != null) {
            VentaLogic.parseEgresoJsonString(egresoAEditar.billetes, egresoAEditar.origen)
        } else {
            Pair(emptyMap(), emptyMap())
        }
    }

    val billetesCajaSeleccionados = remember {
        mutableStateMapOf<Int, Int>().apply { putAll(cajaInicial) }
    }

    val toquitosCajitaSeleccionados = remember {
        mutableStateMapOf<Int, Int>().apply { putAll(cajitaInicial) }
    }

    val montoCajaLong = billetesCajaSeleccionados.entries.sumOf { (denom, cant) ->
        if (cant > 0) cant.toLong() * denom.toLong() else 0L
    }

    val montoCajitaLong = toquitosCajitaSeleccionados.entries.sumOf { (denom, cant) ->
        if (cant > 0) cant.toLong() * denom.toLong() * 10L else 0L
    }

    val montoTotalLong = montoCajaLong + montoCajitaLong

    var errorMensaje by remember { mutableStateOf<String?>(null) }

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
            Text(
                text = if (egresoAEditar == null) "Sacar plata" else "Editar egreso",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val esRetiro = tipoSeleccionado == "RETIRO"

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (esRetiro) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (esRetiro) 2.dp else 1.dp,
                                color = if (esRetiro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { tipoSeleccionado = "RETIRO" }
                        ) {
                            Text(
                                text = "Retiro",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!esRetiro) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (!esRetiro) 2.dp else 1.dp,
                                color = if (!esRetiro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { tipoSeleccionado = "PROVEEDOR" }
                        ) {
                            Text(
                                text = "Pago a proveedor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }

                if (tipoSeleccionado == "PROVEEDOR") {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROVEEDOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (proveedoresRecientes.isNotEmpty()) {
                                    Text(
                                        text = "últimos ▼",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = proveedorTexto,
                                onValueChange = { proveedorTexto = it },
                                placeholder = { Text("Nombre del proveedor") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (proveedoresRecientes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(proveedoresRecientes) { p ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { proveedorTexto = p.nombre }
                                        ) {
                                            Text(
                                                text = p.nombre,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Selector SALE DE: Caja / Cajita (toquitos)
                item {
                    Column {
                        Text(
                            text = "SALE DE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val esCaja = tabActivo == "CAJA"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (esCaja) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (esCaja) 2.dp else 1.dp,
                                    color = if (esCaja) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tabActivo = "CAJA" }
                            ) {
                                val tieneCaja = billetesCajaSeleccionados.any { it.value > 0 }
                                Text(
                                    text = if (tieneCaja) "Caja (${montoCajaLong.toFormatoMonedaARS()})" else "Caja",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (!esCaja) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (!esCaja) 2.dp else 1.dp,
                                    color = if (!esCaja) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tabActivo = "CAJITA" }
                            ) {
                                val tieneCajita = toquitosCajitaSeleccionados.any { it.value > 0 }
                                Text(
                                    text = if (tieneCajita) "Cajita (${montoCajitaLong.toFormatoMonedaARS()})" else "Cajita (toquitos)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Grid de billetes/toquitos que salen según la pestaña activa
                item {
                    Column {
                        val esCajaTab = tabActivo == "CAJA"
                        Text(
                            text = if (esCajaTab) "BILLETES QUE SALEN (CAJA)" else "TOQUITOS QUE SALEN (CAJITA)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val denoms = Denominaciones.LISTA.take(8).map { it.valor }
                        val filas = denoms.chunked(4)

                        val mapaTarget = if (esCajaTab) billetesCajaSeleccionados else toquitosCajitaSeleccionados
                        val mapaDisponible = if (esCajaTab) cajaBilletes else cajitaToquitos

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (fila in filas) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (denom in fila) {
                                        val cantidad = mapaTarget[denom] ?: 0
                                        val tieneValor = cantidad > 0

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (tieneValor) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (tieneValor) 1.5.dp else 1.dp,
                                                color = if (tieneValor) errorColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .combinedClickable(
                                                    onClick = {
                                                        val disp = mapaDisponible[denom] ?: 0
                                                        if (cantidad + 1 <= disp) {
                                                            mapaTarget[denom] = cantidad + 1
                                                            errorMensaje = null
                                                        } else {
                                                            val unidad = if (esCajaTab) "billetes" else "toquitos"
                                                            errorMensaje = "No hay suficientes $unidad de $ $denom"
                                                        }
                                                    },
                                                    onLongClick = {
                                                        val cant = cantidad - 1
                                                        if (cant <= 0) mapaTarget.remove(denom) else mapaTarget[denom] = cant
                                                        errorMensaje = null
                                                    }
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

                                                val labelCant = when {
                                                    cantidad > 0 -> if (esCajaTab) "-$cantidad" else "-$cantidad toq."
                                                    else -> "·"
                                                }

                                                Text(
                                                    text = labelCant,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (tieneValor) errorColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notaTexto,
                        onValueChange = { notaTexto = it },
                        placeholder = { Text("Nota · ej. factura 0004-1532") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }
            }

            if (errorMensaje != null) {
                Text(
                    text = errorMensaje!!,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = errorColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Banner de total que sale
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = errorColor.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val textoDetalleSalida = when {
                    montoCajaLong > 0 && montoCajitaLong > 0 -> "− Sale de caja: ${montoCajaLong.toFormatoMonedaARS()} + cajita: ${montoCajitaLong.toFormatoMonedaARS()} = ${montoTotalLong.toFormatoMonedaARS()}"
                    montoCajitaLong > 0 -> "− Sale de la cajita: ${montoCajitaLong.toFormatoMonedaARS()}"
                    else -> "− Sale de la caja: ${montoCajaLong.toFormatoMonedaARS()}"
                }

                Text(
                    text = textoDetalleSalida,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = errorColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }

            val botonValido = montoTotalLong > 0 && (tipoSeleccionado != "PROVEEDOR" || proveedorTexto.isNotBlank())

            Button(
                onClick = {
                    if (botonValido) {
                        val origenReal = when {
                            montoCajaLong > 0 && montoCajitaLong > 0 -> "MIXTO"
                            montoCajitaLong > 0 -> "CAJITA"
                            else -> "CAJA"
                        }
                        onRegistrarEgreso(
                            tipoSeleccionado,
                            origenReal,
                            billetesCajaSeleccionados.toMap(),
                            toquitosCajitaSeleccionados.toMap(),
                            proveedorTexto.ifBlank { null },
                            notaTexto.ifBlank { null }
                        )
                        onDismiss()
                    }
                },
                enabled = botonValido,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = errorColor,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (tipoSeleccionado == "PROVEEDOR") "Registrar pago" else "Registrar retiro",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size(width = (46 * scale).dp, height = (30 * scale).dp)
)
