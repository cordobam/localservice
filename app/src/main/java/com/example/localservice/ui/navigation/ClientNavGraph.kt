package com.example.localservice.ui.navigation

import android.R.attr.type
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.localservice.ui.screens.client.MyBookingsScreen
import com.example.localservice.ui.screens.client.ProviderDetailScreen
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
    composable(
        route = Screen.ProviderDetail.route,
        arguments = listOf(navArgument("providerId") { type = NavType.StringType })
    ) {
        ProviderDetailScreen(
            onBack = { navController.popBackStack() },
            onBookingCreated = { slug ->
                // Por ahora volvemos a la búsqueda — en Fase 4 navegaremos al Tracking
                navController.popBackStack()
            },
            authViewModel = authViewModel
        )
    }
}
