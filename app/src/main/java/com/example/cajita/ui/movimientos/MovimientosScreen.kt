package com.example.cajita.ui.movimientos

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.VentaLocal
import com.example.cajita.domain.model.MedioPago
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.caja.NuevaVentaBottomSheet
import com.example.cajita.ui.caja.SacarPlataBottomSheet
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.LightError
import com.example.cajita.ui.theme.LightOk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MovimientosScreen(
    viewModel: MovimientosViewModel,
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val okColor = if (isDark) DarkOk else LightOk
    val errorColor = if (isDark) DarkError else LightError

    val listaMovimientos by viewModel.listaMovimientosCombined.collectAsState()
    val filtroSeleccionado by viewModel.filtroSeleccionado.collectAsState()
    val totalesSummary by viewModel.totalesSummary.collectAsState()
    val totalCount by viewModel.totalOperacionesCount.collectAsState()
    val conteos by viewModel.conteos.collectAsState()
    val proveedoresRecientes by viewModel.proveedoresRecientes.collectAsState()

    var itemExpandidoId by remember { mutableStateOf<String?>(null) }
    var ventaAEditar by remember { mutableStateOf<VentaLocal?>(null) }
    var egresoAEditar by remember { mutableStateOf<EgresoLocal?>(null) }
    var itemABorrar by remember { mutableStateOf<MovimientoItem?>(null) }

    val cajaMap = remember(conteos) { conteos.associate { it.denominacion to it.billetesCaja } }
    val cajitaMap = remember(conteos) { conteos.associate { it.denominacion to it.toquitosCajita } }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Digital",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = totalesSummary.first.toFormatoMonedaARS(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "Efectivo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = totalesSummary.second.toFormatoMonedaARS(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Salidas",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "- ${totalesSummary.third.toFormatoMonedaARS()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = errorColor
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // BARRA SUPERIOR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Movimientos",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "$totalCount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // FILTROS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filtros = listOf("Todos", "Efectivo", "Digital", "Salidas")
                    filtros.forEach { f ->
                        val seleccionado = filtroSeleccionado == f
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (seleccionado) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (seleccionado) 1.5.dp else 1.dp,
                                color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable { viewModel.seleccionarFiltro(f) }
                        ) {
                            Text(
                                text = f,
                                fontSize = 13.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                color = if (seleccionado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LISTA DE MOVIMIENTOS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaMovimientos, key = { it.id }) { item ->
                        val estaExpandido = itemExpandidoId == item.id

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    itemExpandidoId = if (estaExpandido) null else item.id
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (bgColor, iconChar, tagLabel, tagBg, isPositive) = when (item) {
                                        is MovimientoItem.Venta -> {
                                            when (item.venta.medio) {
                                                MedioPago.EFECTIVO -> Tuple5(okColor, "$", "Efectivo", okColor.copy(alpha = 0.15f), true)
                                                MedioPago.DEBITO -> Tuple5(Color(0xFF2B80FF), "▰", "Débito", Color(0xFF2B80FF).copy(alpha = 0.15f), true)
                                                MedioPago.QR_TRANSFERENCIA -> Tuple5(Color(0xFF9C42F5), "▦", "QR / Transf.", Color(0xFF9C42F5).copy(alpha = 0.15f), true)
                                                else -> Tuple5(Color(0xFFE942A5), "◆", "Crédito", Color(0xFFE942A5).copy(alpha = 0.15f), true)
                                            }
                                        }
                                        is MovimientoItem.Egreso -> Tuple5(errorColor, "↑", if (item.egreso.tipo == "PROVEEDOR") "Proveedor" else "Retiro", errorColor.copy(alpha = 0.15f), false)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(bgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = iconChar,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val titulo = when (item) {
                                                is MovimientoItem.Venta -> {
                                                    val v = item.venta
                                                    when {
                                                        v.montoDigital > 0 && v.efectivoSalida > 0 -> "Venta + extracción"
                                                        v.montoDigital > 0 && v.efectivoEntrada > 0 -> "Venta mixta"
                                                        else -> "Venta"
                                                    }
                                                }
                                                is MovimientoItem.Egreso -> {
                                                    if (item.egreso.tipo == "PROVEEDOR") "Pago a proveedor" else "Retiro"
                                                }
                                            }

                                            Text(
                                                text = titulo,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = tagBg
                                            ) {
                                                Text(
                                                    text = tagLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = bgColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        val detalleTexto = when (item) {
                                            is MovimientoItem.Venta -> {
                                                val v = item.venta
                                                when {
                                                    v.medio == MedioPago.EFECTIVO -> "entró ${v.efectivoEntrada.toFormatoMonedaARS()} · vuelto ${v.efectivoSalida.toFormatoMonedaARS()}"
                                                    v.efectivoSalida > 0 -> "${MedioPago.toLabel(v.medio)} ${v.montoDigital.toFormatoMonedaARS()} · salió ${v.efectivoSalida.toFormatoMonedaARS()}"
                                                    v.efectivoEntrada > 0 -> "${MedioPago.toLabel(v.medio)} ${v.montoDigital.toFormatoMonedaARS()} · entró ${v.efectivoEntrada.toFormatoMonedaARS()}"
                                                    else -> "${MedioPago.toLabel(v.medio)} ${v.totalVenta.toFormatoMonedaARS()}"
                                                }
                                            }
                                            is MovimientoItem.Egreso -> {
                                                val e = item.egreso
                                                val deDonde = when (e.origen) {
                                                    "CAJITA" -> "De la cajita"
                                                    "MIXTO" -> "De caja y cajita"
                                                    else -> "De la caja"
                                                }
                                                val prov = if (!e.proveedor.isNullOrBlank()) "${e.proveedor} · " else ""
                                                "$prov$deDonde"
                                            }
                                        }

                                        Text(
                                            text = detalleTexto,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val montoStr = when (item) {
                                            is MovimientoItem.Venta -> item.venta.totalVenta.toFormatoMonedaARS()
                                            is MovimientoItem.Egreso -> "- ${item.egreso.monto.toFormatoMonedaARS()}"
                                        }

                                        Text(
                                            text = montoStr,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isPositive) MaterialTheme.colorScheme.onSurface else errorColor
                                        )

                                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        Text(
                                            text = sdf.format(Date(item.ocurridoEn)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (estaExpandido) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    when (item) {
                                                        is MovimientoItem.Venta -> ventaAEditar = item.venta
                                                        is MovimientoItem.Egreso -> egresoAEditar = item.egreso
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "✏ Editar",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = errorColor.copy(alpha = 0.12f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { itemABorrar = item }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🗑 Borrar",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = errorColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (ventaAEditar != null) {
        NuevaVentaBottomSheet(
            cajaBilletes = cajaMap,
            ventaAEditar = ventaAEditar,
            onDismiss = { ventaAEditar = null },
            onGuardarVenta = { medio, total, mDigital, bEntrada, bSalida, nota ->
                viewModel.editarVenta(
                    ventaActual = ventaAEditar!!,
                    nuevoMedio = medio,
                    nuevoTotalVenta = total,
                    nuevoMontoDigital = mDigital,
                    nuevosBilletesEntrada = bEntrada,
                    nuevosBilletesSalida = bSalida,
                    nuevaNota = nota
                )
                ventaAEditar = null
            }
        )
    }

    if (egresoAEditar != null) {
        SacarPlataBottomSheet(
            cajaBilletes = cajaMap,
            cajitaToquitos = cajitaMap,
            proveedoresRecientes = proveedoresRecientes,
            egresoAEditar = egresoAEditar,
            onDismiss = { egresoAEditar = null },
            onRegistrarEgreso = { tipo, origen, bCaja, tCajita, proveedor, nota ->
                viewModel.editarEgreso(
                    egresoActual = egresoAEditar!!,
                    nuevoTipo = tipo,
                    nuevoOrigen = origen,
                    nuevosBilletesCaja = bCaja,
                    nuevosToquitosCajita = tCajita,
                    nuevoProveedor = proveedor,
                    nuevaNota = nota
                )
                egresoAEditar = null
            }
        )
    }

    if (itemABorrar != null) {
        AlertDialog(
            onDismissRequest = { itemABorrar = null },
            title = { Text("¿Borrar movimiento?", fontWeight = FontWeight.Bold) },
            text = { Text("Se revertirán los billetes correspondientes en la caja/cajita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val item = itemABorrar!!
                        when (item) {
                            is MovimientoItem.Venta -> viewModel.borrarVenta(item.venta)
                            is MovimientoItem.Egreso -> viewModel.borrarEgreso(item.egreso)
                        }
                        itemABorrar = null
                    }
                ) {
                    Text("Borrar", fontWeight = FontWeight.Bold, color = errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemABorrar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
