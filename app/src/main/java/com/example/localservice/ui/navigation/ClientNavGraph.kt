package com.example.localservice.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.localservice.ui.screens.client.MyBookingsScreen
import com.example.localservice.ui.screens.client.ProviderDetailScreen
import com.example.localservice.ui.screens.client.SearchScreen
import com.example.localservice.ui.viewmodel.AuthViewModel

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

    composable(
        route = Screen.ProviderDetail.route,
        arguments = listOf(navArgument("providerId") { type = NavType.StringType })
    ) {
        ProviderDetailScreen(
            onBack = { navController.popBackStack() },
            onBookingCreated = { _ ->
                // Navega a Mis Pedidos después de crear la solicitud
                navController.navigate(Screen.MyBookings.route)
            },
            authViewModel = authViewModel
        )
    }

    composable(Screen.MyBookings.route) {
        MyBookingsScreen(
            onBack = { navController.popBackStack() },
            onNavigateToTracking = { slug ->
                navController.navigate(Screen.Tracking.createRoute(slug))
            },
            onNavigateToChat = { bookingId, providerName ->
                navController.navigate(Screen.Chat.createRoute(bookingId, providerName))
            },
            onNavigateToReview = { providerUid, providerName ->
                navController.navigate(Screen.Review.createRoute(providerUid, providerName))
            },
            authViewModel = authViewModel
        )
    }
}
