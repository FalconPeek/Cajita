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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.SyncStatus
import com.example.cajita.ui.theme.DarkOk
import com.example.cajita.ui.theme.DarkWarn
import com.example.cajita.ui.theme.LightOk
import com.example.cajita.ui.theme.LightWarn

@Composable
fun SyncStatusChip(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (bg, fg, text) = when (status) {
        is SyncStatus.Enviado -> {
            val green = if (isDark) DarkOk else LightOk
            Triple(green.copy(alpha = 0.15f), green, "● Enviado")
        }
        is SyncStatus.SinConexion -> {
            val warn = if (isDark) DarkWarn else LightWarn
            Triple(warn.copy(alpha = 0.15f), warn, "● Sin conexión · ${status.pendientes} pendientes")
        }
    }

    Row(
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
