package com.example.cajita.ui.cerrar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun CerrarCajaScreen(
    viewModel: CerrarCajaViewModel,
    onNavigateBack: () -> Unit,
    onCierreConfirmado: (String) -> Unit
) {
    val pasoActual by viewModel.pasoActual.collectAsState()

    when (pasoActual) {
        1 -> {
            CerrarCajaPaso1Screen(
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onSiguienteClick = { viewModel.irAPaso(2) }
            )
        }
        2 -> {
            CerrarCajaPaso2Screen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.irAPaso(1) },
                onSiguienteClick = { viewModel.irAPaso(3) }
            )
        }
        3 -> {
            ConfirmarCierreScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.irAPaso(2) },
                onCierreConfirmado = onCierreConfirmado
            )
        }
    }
}
