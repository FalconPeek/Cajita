package com.example.cajita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.toFormatoMonedaARS
import com.example.cajita.ui.theme.DarkError
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightError
import com.example.cajita.ui.theme.LightOk
import com.example.cajita.ui.theme.LightWarn

@Composable
fun DifferenceBadge(
    diferencia: Long,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val okColor = if (isDark) DarkOk else LightOk
    val warnColor = if (isDark) DarkWarn else LightWarn
    val errorColor = if (isDark) DarkError else LightError

    val (bg, fg, text) = when {
        diferencia == 0L -> Triple(okColor.copy(alpha = 0.15f), okColor, "✓ Cuadra $ 0")
        diferencia > 0L -> Triple(warnColor.copy(alpha = 0.15f), warnColor, "▲ Sobran ${diferencia.toFormatoMonedaARS()}")
        else -> Triple(errorColor.copy(alpha = 0.15f), errorColor, "▲ Faltan ${Math.abs(diferencia).toFormatoMonedaARS()}")
    }

    Row(
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
