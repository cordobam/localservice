package com.example.localservice.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.localservice.ui.screens.provider.DashboardScreen
import com.example.localservice.ui.viewmodel.AuthViewModel

fun NavGraphBuilder.providerNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Screen.Dashboard.route) {
        DashboardScreen(
            onNavigateToRequests = {
                navController.navigate(Screen.IncomingRequests.route)
            },
            onNavigateToProject = { bookingId ->
                navController.navigate(Screen.ProjectDetail.createRoute(bookingId))
            },
            onNavigateToProfile = {
                navController.navigate(Screen.MyProfile.route)
            },
            onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            authViewModel = authViewModel
        )
    }

    // Próximas pantallas del prestador se agregan acá en Fase 4:
    // StageEditorScreen, BudgetBuilderScreen, MyProfileScreen, etc.
}
