package com.example.cajita.ui.cerrada

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.SyncStatus
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.components.SyncStatusChip
import com.example.cajita.ui.inicio.CheckIconCanvas
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightError
import com.example.cajita.ui.theme.LightOk
import com.example.cajita.ui.theme.LightWarn

@Composable
fun CajaCerradaScreen(
    cierreId: String,
    viewModel: CajaCajaCerradaViewModelAlias,
    onListoClick: () -> Unit
) {
    val cierre by viewModel.cierre.collectAsState()
    val conteos by viewModel.conteos.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    LaunchedEffect(cierreId) {
        viewModel.cargarCierre(cierreId)
    }

    val isDark = isSystemInDarkTheme()
    val okColor = if (isDark) DarkOk else LightOk
    val warnColor = if (isDark) DarkWarn else LightWarn
    val errorColor = if (isDark) DarkError else LightError

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Big Checkmark
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(okColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CheckIconCanvas(color = okColor, modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Caja cerrada",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Turno guardado con éxito",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                val posberry = cierre?.totalPosberry ?: 0L
                val contado = viewModel.calcularTotalContado(conteos)
                val totalDigital = cierre?.totalDigital ?: 0L
                val totalGeneralTurno = contado + totalDigital
                val diferencia = contado - posberry

                val (diffFg, diffText) = when {
                    diferencia == 0L -> Pair(okColor, "✓ Cuadra justo")
                    diferencia > 0L -> Pair(warnColor, "Sobran ${diferencia.toFormatoMonedaARS()}")
                    else -> Pair(errorColor, "Faltan ${Math.abs(diferencia).toFormatoMonedaARS()}")
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Turno", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(totalGeneralTurno.toFormatoMonedaARS(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Efectivo", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(contado.toFormatoMonedaARS(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Digital", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(totalDigital.toFormatoMonedaARS(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("POSBerry (Efectivo)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(posberry.toFormatoMonedaARS(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diferencia Efectivo", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(diffText, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = diffFg)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Envío Supabase", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            SyncStatusChip(status = syncStatus)
                        }
                    }
                }

                if (syncStatus is SyncStatus.SinConexion) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Está guardado en el teléfono. Se envía solo cuando vuelva la conexión.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Button(
                onClick = onListoClick,
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
                    text = "Listo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

typealias CajaCajaCerradaViewModelAlias = CajaCerradaViewModel
