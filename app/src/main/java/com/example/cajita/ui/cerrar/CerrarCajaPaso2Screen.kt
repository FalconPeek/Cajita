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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.Denominaciones
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightError
import com.example.cajita.ui.theme.LightOk
import com.example.cajita.ui.theme.LightWarn

@Composable
fun CerrarCajaPaso2Screen(
    viewModel: CerrarCajaViewModel,
    onNavigateBack: () -> Unit,
    onSiguienteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val okColor = if (isDark) DarkOk else LightOk
    val warnColor = if (isDark) DarkWarn else LightWarn
    val errorColor = if (isDark) DarkError else LightError

    val conteos by viewModel.conteos.collectAsState()
    val posberryTexto by viewModel.posberryTexto.collectAsState()
    val egresosActivos by viewModel.egresosActivos.collectAsState()
    val mostrarTodosBilletes by viewModel.mostrarTodosBilletes.collectAsState()

    val totalCaja = viewModel.calcularTotalCaja()
    val totalCajita = viewModel.calcularTotalCajita()
    val totalToquitos = viewModel.calcularTotalToquitos()
    val totalContado = totalCaja + totalCajita

    val retirosList = egresosActivos.filter { it.tipo == "RETIRO" }
    val retirosCount = retirosList.size
    val retirosMonto = retirosList.sumOf { it.monto }

    val proveedoresList = egresosActivos.filter { it.tipo == "PROVEEDOR" }
    val proveedoresCount = proveedoresList.size
    val proveedoresMonto = proveedoresList.sumOf { it.monto }

    val posberryLong = viewModel.calcularPosberryLong()
    val diferencia = totalContado - posberryLong

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
                            text = "Paso 2 de 2",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Arqueo de efectivo",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar 100%
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
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // TARJETA EFECTIVO CONTADO
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("En caja", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(totalCaja.toFormatoMonedaARS(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("En cajita · $totalToquitos toquitos", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(totalCajita.toFormatoMonedaARS(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Efectivo contado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(totalContado.toFormatoMonedaARS(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (mostrarTodosBilletes) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Denominaciones.LISTA.forEach { item ->
                                        val c = conteos.find { it.denominacion == item.valor }
                                        val b = c?.billetesCaja ?: 0
                                        val t = c?.toquitosCajita ?: 0
                                        val sub = (b + t * 10L) * item.valor
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("$ ${item.valor}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Caja: $b · Cajita: $t toq. = ${sub.toFormatoMonedaARS()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (mostrarTodosBilletes) "Ocultar detalle por billete" else "Ver detalle por billete · Corregir",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { viewModel.toggleMostrarTodosBilletes() }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // TARJETA SALIÓ EN EL TURNO
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "SALIÓ EN EL TURNO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Retiros · $retirosCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("- ${retirosMonto.toFormatoMonedaARS()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = errorColor)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Proveedores · $proveedoresCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("- ${proveedoresMonto.toFormatoMonedaARS()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = errorColor)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Si POSBerry no los registra, sumalos al comparar.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // EFECTIVO SEGÚN POSBERRY
                item {
                    Column {
                        Text(
                            text = "EFECTIVO SEGÚN POSBERRY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = posberryTexto,
                            onValueChange = { viewModel.onPosberryChanged(it) },
                            prefix = { Text("$ ", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                            placeholder = { Text("0", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
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
                                .height(58.dp)
                        )
                    }
                }

                // TARJETA DE DIFERENCIA
                item {
                    val (diffBg, diffFg, diffLabel) = when {
                        diferencia == 0L -> Triple(okColor.copy(alpha = 0.15f), okColor, "✓ Cuadra")
                        diferencia > 0L -> Triple(warnColor.copy(alpha = 0.15f), warnColor, "▲ Sobran")
                        else -> Triple(errorColor.copy(alpha = 0.15f), errorColor, "▲ Faltan")
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = diffBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = diffLabel,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = diffFg
                            )

                            Text(
                                text = Math.abs(diferencia).toFormatoMonedaARS(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = diffFg
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // BOTÓN SIGUIENTE / VER RESUMEN
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
                        text = "Ver resumen y confirmar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
