package com.example.cajita.ui.inicio

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.theme.AccentToquitos
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.LightOk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    onNavigateToAbrirTurno: () -> Unit,
    onNavigateToCaja: () -> Unit
) {
    val turnoAbierto by viewModel.turnoAbierto.collectAsState()
    val ultimoCierre by viewModel.ultimoCierre.collectAsState()
    val cajeroUltimoCierre by viewModel.cajeroUltimoCierre.collectAsState()

    LaunchedEffect(turnoAbierto) {
        if (turnoAbierto != null) {
            onNavigateToCaja()
        }
    }

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
                CajitaLogoGraphic()

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Cajita",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cierre de caja sin contar dos veces",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                val isDark = isSystemInDarkTheme()
                val okColor = if (isDark) DarkOk else LightOk

                val cierre = ultimoCierre
                if (cierre != null) {
                    val sdf = SimpleDateFormat("EEEE HH:mm", Locale.getDefault())
                    val fechaFormateada = sdf.format(Date(cierre.cerradoEn))
                    val cajero = cajeroUltimoCierre?.trim()
                    val textoSubheader = if (!cajero.isNullOrEmpty()) "$cajero · Cierre registrado" else "Cierre registrado"

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(okColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CheckIconCanvas(color = okColor, modifier = Modifier.size(22.dp))
                            }

                            Spacer(modifier = Modifier.size(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Último cierre · $fechaFormateada",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = textoSubheader,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = cierre.totalPosberry.toFormatoMonedaARS(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = okColor
                            )
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Estás por abrir la caja por primera vez",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No hay ningún cierre anterior registrado",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateToAbrirTurno,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text(
                        text = "Abrir turno",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No hay ningún turno abierto en este teléfono",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CheckIconCanvas(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.8f, h * 0.25f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
private fun CajitaLogoGraphic() {
    Box(
        modifier = Modifier
            .size(160.dp, 120.dp)
            .background(Color(0xFF1E5238), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp, 60.dp)
                    .background(Color(0xFF2E724E), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(16.dp, 36.dp).background(Color(0xFF4CC38A), RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.size(16.dp, 36.dp).background(Color(0xFF4CC38A), RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.size(16.dp, 36.dp).background(Color(0xFF4CC38A), RoundedCornerShape(4.dp)))
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 4.dp)
                .size(42.dp)
                .background(AccentToquitos, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CheckIconCanvas(color = Color(0xFF0F1F17), modifier = Modifier.size(20.dp))
        }
    }
}
