package com.example.cajita.ui.caja

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.Denominaciones
import com.example.cajita.domain.model.MedioPago
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.components.DenominationChip
import com.example.cajita.ui.theme.AccentToquitos
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.LightError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CajaScreen(
    viewModel: CajaViewModel,
    onNavigateToCerrarCaja: () -> Unit,
    onNavigateToMovimientos: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val errorColor = if (isDark) DarkError else LightError

    val turno by viewModel.turnoAbierto.collectAsState()
    val conteos by viewModel.conteos.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val ultimoMensajeUndo by viewModel.ultimoMensajeUndo.collectAsState()
    val denominacionAEditar by viewModel.denominacionAEditar.collectAsState()
    val operacionesCount by viewModel.operacionesCount.collectAsState()
    val ventasActivas by viewModel.ventasActivas.collectAsState()
    val proveedoresRecientes by viewModel.proveedoresRecientes.collectAsState()

    var mostrarNuevaVentaSheet by remember { mutableStateOf(false) }
    var mostrarSacarPlataSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var denominacionParaSacarToquito by remember { mutableStateOf<Int?>(null) }

    val (totalCaja, totalCajita, totalGeneral) = viewModel.calcularTotales()
    val totalToquitos = viewModel.calcularTotalToquitosCount()

    // Cálculo de ventas digitales
    val ventasDigitalesList = ventasActivas.filter { it.medio != MedioPago.EFECTIVO }
    val cantidadVentasDigitales = ventasDigitalesList.size
    val totalMontoDigital = ventasDigitalesList.sumOf { it.montoDigital }

    fun formatearMiles(monto: Long): String {
        return if (monto >= 1000) {
            val enMiles = monto / 1000.0
            String.format(Locale.getDefault(), "%.1fK", enMiles).replace(".0K", "K").replace(",", ".")
        } else {
            monto.toString()
        }
    }

    val debitoTotal = ventasDigitalesList.filter { it.medio == MedioPago.DEBITO }.sumOf { it.montoDigital }
    val qrTotal = ventasDigitalesList.filter { it.medio == MedioPago.QR_TRANSFERENCIA }.sumOf { it.montoDigital }
    val creditoTotal = ventasDigitalesList.filter { it.medio == MedioPago.CREDITO }.sumOf { it.montoDigital }

    LaunchedEffect(ultimoMensajeUndo) {
        val mensaje = ultimoMensajeUndo
        if (mensaje != null) {
            val result = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = "DESHACER",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.deshacer()
            } else {
                viewModel.limpiarMensajeUndo()
            }
        }
    }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onNavigateToCerrarCaja,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Cerrar caja →",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                val horaApertura = if (turno != null) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    sdf.format(Date(turno!!.abiertoEn))
                } else ""

                val nombreCajero = if (!turno?.cajero.isNullOrBlank()) turno!!.cajero else "Cajero"
                val textoTurno = if (horaApertura.isNotEmpty()) "Turno de $nombreCajero · desde $horaApertura" else "Turno de $nombreCajero"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = textoTurno,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Caja",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onNavigateToMovimientos() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "☰ $operacionesCount",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HERO CARD EFECTIVO
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
                                        text = "EFECTIVO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF4CC38A))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Enviado",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = totalGeneral.toFormatoMonedaARS(),
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
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Caja",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = totalCaja.toFormatoMonedaARS(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Cajita · $totalToquitos toq.",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = totalCajita.toFormatoMonedaARS(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TARJETA VENTAS DIGITALES
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "VENTAS DIGITALES · $cantidadVentasDigitales",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = totalMontoDigital.toFormatoMonedaARS(),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "● Déb ${formatearMiles(debitoTotal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "● QR ${formatearMiles(qrTotal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "● Créd ${formatearMiles(creditoTotal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FILA DE BOTONES DE ACCIÓN: + Venta y - Retiro / Prov.
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { mostrarNuevaVentaSheet = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text(
                                    text = "＋ Venta",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable { mostrarSacarPlataSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "− Retiro / Prov.",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = errorColor
                                    )
                                }
                            }
                        }
                    }

                    // GRILLA DE BILLETES
                    items(Denominaciones.LISTA) { item ->
                        val conteo = conteos.find { it.denominacion == item.valor }
                        val billetesCaja = conteo?.billetesCaja ?: 0
                        val toquitosCajita = conteo?.toquitosCajita ?: 0

                        val canMoveToCajita = billetesCaja >= 10

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DenominationChip(denominacion = item.valor)

                                Spacer(modifier = Modifier.width(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                triggerHapticFeedback()
                                                viewModel.modificarBilletes(item.valor, -1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.abrirEditarModal(item.valor) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = billetesCaja.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                triggerHapticFeedback()
                                                viewModel.modificarBilletes(item.valor, 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (canMoveToCajita) AccentToquitos.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .combinedClickable(
                                                enabled = canMoveToCajita || toquitosCajita > 0,
                                                onClick = {
                                                    if (canMoveToCajita) {
                                                        triggerHapticFeedback()
                                                        viewModel.moverACajita(item.valor)
                                                    } else if (toquitosCajita > 0) {
                                                        denominacionParaSacarToquito = item.valor
                                                    }
                                                },
                                                onLongClick = {
                                                    if (toquitosCajita > 0) {
                                                        triggerHapticFeedback()
                                                        denominacionParaSacarToquito = item.valor
                                                    }
                                                }
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "→ cajita",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canMoveToCajita) AccentToquitos else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }

                                    Text(
                                        text = "$toquitosCajita toq.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (mostrarNuevaVentaSheet) {
        val cajaMap = conteos.associate { it.denominacion to it.billetesCaja }
        NuevaVentaBottomSheet(
            cajaBilletes = cajaMap,
            onDismiss = { mostrarNuevaVentaSheet = false },
            onGuardarVenta = { medio, total, mDigital, bEntrada, bSalida, nota ->
                viewModel.guardarNuevaVenta(medio, total, mDigital, bEntrada, bSalida, nota)
            }
        )
    }

    if (mostrarSacarPlataSheet) {
        val cajaMap = conteos.associate { it.denominacion to it.billetesCaja }
        val cajitaMap = conteos.associate { it.denominacion to it.toquitosCajita }
        SacarPlataBottomSheet(
            cajaBilletes = cajaMap,
            cajitaToquitos = cajitaMap,
            proveedoresRecientes = proveedoresRecientes,
            onDismiss = { mostrarSacarPlataSheet = false },
            onRegistrarEgreso = { tipo, origen, billetes, proveedor, nota ->
                viewModel.guardarNuevoEgreso(tipo, origen, billetes, proveedor, nota)
            }
        )
    }

    if (denominacionAEditar != null) {
        val denom = denominacionAEditar!!
        val conteoActual = conteos.find { it.denominacion == denom }?.billetesCaja ?: 0

        EditarCantidadBottomSheet(
            denominacion = denom,
            cantidadActual = conteoActual,
            onDismiss = { viewModel.cerrarEditarModal() },
            onGuardar = { nueva ->
                viewModel.fijarCantidad(denom, nueva)
            }
        )
    }

    if (denominacionParaSacarToquito != null) {
        val denom = denominacionParaSacarToquito!!
        AlertDialog(
            onDismissRequest = { denominacionParaSacarToquito = null },
            title = { Text("¿Sacar toquito?", fontWeight = FontWeight.Bold) },
            text = { Text("Se sumarán 10 billetes de $ $denom a la caja y se restará 1 toquito de la cajita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sacarDeCajita(denom)
                        denominacionParaSacarToquito = null
                    }
                ) {
                    Text("← Sacar toquito", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { denominacionParaSacarToquito = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
