package com.example.cajita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajita.domain.model.Denominaciones

@Composable
fun DenominationChip(
    denominacion: Int,
    modifier: Modifier = Modifier
) {
    val item = Denominaciones.getItem(denominacion)
    Box(
        modifier = modifier
            .size(width = 46.dp, height = 30.dp)
            .background(color = item.colorHex, shape = RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.etiquetaChip,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
