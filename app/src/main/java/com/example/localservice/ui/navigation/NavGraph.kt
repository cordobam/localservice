package com.example.localservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.localservice.domain.model.UserRole
import com.example.localservice.ui.screens.auth.LoginScreen
import com.example.localservice.ui.screens.auth.RegisterScreen
import com.example.localservice.ui.screens.auth.RolePickerScreen
import com.example.localservice.ui.screens.client.ClientMainScreen
import com.example.localservice.ui.screens.client.ProviderDetailScreen
import com.example.localservice.ui.screens.client.TrackingScreen
import com.example.localservice.ui.screens.provider.DashboardScreen
import com.example.localservice.ui.screens.provider.ProviderSetupScreen
import com.example.localservice.ui.screens.provider.StageEditorScreen
import com.example.localservice.ui.viewmodel.AuthViewModel

import android.net.Uri
import androidx.navigation.navArgument
import com.example.localservice.ui.screens.client.ChatScreen
import com.example.localservice.ui.screens.client.ReviewScreen
import com.example.localservice.ui.screens.provider.ProviderProfileScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination = when {
        uiState.isLoggedIn && uiState.currentUser?.role == UserRole.CLIENT   -> Screen.ClientMain.route
        uiState.isLoggedIn && uiState.currentUser?.role == UserRole.PROVIDER -> Screen.Dashboard.route
        else -> Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { role ->
                    val dest = if (role == UserRole.CLIENT) Screen.ClientMain.route else Screen.Dashboard.route
                    navController.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToRolePicker = { navController.navigate(Screen.RolePicker.route) }
            )
        }

        composable(Screen.RolePicker.route) {
            RolePickerScreen(
                viewModel = authViewModel,
                onRoleSelected = { role ->
                    val dest = if (role == UserRole.PROVIDER) Screen.ProviderSetup.route else Screen.ClientMain.route
                    navController.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                }
            )
        }

        composable(Screen.ProviderSetup.route) {
            ProviderSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ProviderSetup.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // Cliente — tab bar
        composable(Screen.ClientMain.route) {
            ClientMainScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToProviderDetail = { providerId ->
                    navController.navigate(Screen.ProviderDetail.createRoute(providerId))
                },
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

        composable(
            route = Screen.ProviderDetail.route,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) {
            ProviderDetailScreen(
                onBack = { navController.popBackStack() },
                onBookingCreated = { _ -> navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.Tracking.route,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStack ->
            val slug = backStack.arguments?.getString("slug") ?: ""
            TrackingScreen(slug = slug, onBack = { navController.popBackStack() })
        }

        // Prestador
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToRequests = { },
                onNavigateToProject = { bookingId ->
                    navController.navigate(Screen.StageEditor.createRoute(bookingId))
                },
                onNavigateToProfile = { },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.StageEditor.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStack ->
            val bookingId = backStack.arguments?.getString("projectId") ?: ""
            StageEditorScreen(
                bookingId = bookingId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "chat/{bookingId}/{providerName}",
            arguments = listOf(
                navArgument("bookingId")    { type = NavType.StringType },
                navArgument("providerName") { type = NavType.StringType }
            )
        ) { backStack ->
            val bookingId    = backStack.arguments?.getString("bookingId") ?: ""
            val providerName = backStack.arguments?.getString("providerName") ?: ""
            ChatScreen(
                bookingId    = bookingId,
                providerName = providerName,
                onBack       = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable(
            route = "review/{providerUid}/{providerName}",
            arguments = listOf(
                navArgument("providerUid")  { type = NavType.StringType },
                navArgument("providerName") { type = NavType.StringType }
            )
        ) { backStack ->
            val providerUid  = backStack.arguments?.getString("providerUid") ?: ""
            val providerName = backStack.arguments?.getString("providerName") ?: ""
            ReviewScreen(
                providerUid  = providerUid,
                providerName = providerName,
                onBack       = { navController.popBackStack() },
                onSubmitted  = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable("provider_profile") {
            ProviderProfileScreen(
                onBack        = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
    }
}
