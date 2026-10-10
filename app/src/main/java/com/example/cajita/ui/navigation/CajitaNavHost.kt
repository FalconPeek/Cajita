package com.example.cajita.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cajita.ui.abrir.AbrirTurnoScreen
import com.example.cajita.ui.abrir.AbrirTurnoViewModel
import com.example.cajita.ui.caja.CajaScreen
import com.example.cajita.ui.caja.CajaViewModel
import com.example.cajita.ui.cerrada.CajaCerradaScreen
import com.example.cajita.ui.cerrada.CajaCerradaViewModel
import com.example.cajita.ui.cerrar.CerrarCajaScreen
import com.example.cajita.ui.cerrar.CerrarCajaViewModel
import com.example.cajita.ui.inicio.InicioScreen
import com.example.cajita.ui.inicio.InicioViewModel
import com.example.cajita.ui.movimientos.MovimientosScreen
import com.example.cajita.ui.movimientos.MovimientosViewModel

sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")
    data object AbrirTurno : Screen("abrir_turno")
    data object Caja : Screen("caja")
    data object Movimientos : Screen("movimientos")
    data object CerrarCaja : Screen("cerrar_caja")
    data object CajaCerrada : Screen("caja_cerrada/{cierreId}") {
        fun createRoute(cierreId: String) = "caja_cerrada/$cierreId"
    }
}

@Composable
fun CajitaNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inicio.route
    ) {
        composable(Screen.Inicio.route) {
            val vm: InicioViewModel = viewModel()
            InicioScreen(
                viewModel = vm,
                onNavigateToAbrirTurno = {
                    navController.navigate(Screen.AbrirTurno.route)
                },
                onNavigateToCaja = {
                    navController.navigate(Screen.Caja.route) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AbrirTurno.route) {
            val vm: AbrirTurnoViewModel = viewModel()
            AbrirTurnoScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onTurnoEmpezado = {
                    navController.navigate(Screen.Caja.route) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Caja.route) {
            val vm: CajaViewModel = viewModel()
            CajaScreen(
                viewModel = vm,
                onNavigateToCerrarCaja = {
                    navController.navigate(Screen.CerrarCaja.route)
                },
                onNavigateToMovimientos = {
                    navController.navigate(Screen.Movimientos.route)
                }
            )
        }

        composable(Screen.Movimientos.route) {
            val vm: MovimientosViewModel = viewModel()
            MovimientosScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CerrarCaja.route) {
            val vm: CerrarCajaViewModel = viewModel()
            CerrarCajaScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onCierreConfirmado = { cierreId ->
                    navController.navigate(Screen.CajaCerrada.createRoute(cierreId)) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CajaCerrada.route,
            arguments = listOf(navArgument("cierreId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cierreId = backStackEntry.arguments?.getString("cierreId") ?: ""
            val vm: CajaCerradaViewModel = viewModel()
            CajaCerradaScreen(
                cierreId = cierreId,
                viewModel = vm,
                onListoClick = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
