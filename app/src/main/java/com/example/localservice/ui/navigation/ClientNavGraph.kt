package com.example.localservice.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.localservice.ui.screens.client.MyBookingsScreen
import com.example.localservice.ui.screens.client.SearchScreen
import com.example.localservice.ui.viewmodel.AuthViewModel

// Extensión del NavGraphBuilder para mantener el NavGraph principal limpio.
// Todas las rutas del cliente van acá.
fun NavGraphBuilder.clientNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Screen.Search.route) {
        SearchScreen(
            onNavigateToProviderDetail = { providerId ->
                navController.navigate(Screen.ProviderDetail.createRoute(providerId))
            },
            onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.MyBookings.route) {
        MyBookingsScreen(
            onNavigateToTracking = { projectId ->
                navController.navigate(Screen.Tracking.createRoute(projectId))
            },
            onBack = { navController.popBackStack() }
        )
    }

    // Próximas pantallas del cliente se agregan acá:
    // ProviderDetailScreen, BookingScreen, TrackingScreen, etc.
}
