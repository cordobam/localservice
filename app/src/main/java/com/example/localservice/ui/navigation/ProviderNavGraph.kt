package com.servilocal.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.localservice.ui.navigation.Screen
import com.example.localservice.ui.screens.provider.DashboardScreen
import com.example.localservice.ui.viewmodel.AuthViewModel

// Todas las rutas del prestador van acá.
fun NavGraphBuilder.providerNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Screen.Dashboard.route) {
        DashboardScreen(
            onNavigateToRequests = {
                navController.navigate(Screen.IncomingRequests.route)
            },
            onNavigateToProject = { projectId ->
                navController.navigate(Screen.ProjectDetail.createRoute(projectId))
            },
            onNavigateToProfile = {
                navController.navigate(Screen.MyProfile.route)
            },
            onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Próximas pantallas del prestador se agregan acá:
    // IncomingRequestsScreen, ProjectDetailScreen, StageEditorScreen, etc.
}
