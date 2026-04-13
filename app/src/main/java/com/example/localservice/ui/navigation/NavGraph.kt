package com.example.localservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localservice.domain.model.UserRole
import com.example.localservice.ui.screens.auth.LoginScreen
import com.example.localservice.ui.screens.auth.RegisterScreen
import com.example.localservice.ui.screens.auth.RolePickerScreen
import com.example.localservice.ui.screens.provider.ProviderSetupScreen
import com.example.localservice.ui.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination = when {
        uiState.isLoggedIn && uiState.currentUser?.role == UserRole.CLIENT   -> Screen.Search.route
        uiState.isLoggedIn && uiState.currentUser?.role == UserRole.PROVIDER -> Screen.Dashboard.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // --- Auth ---
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { role ->
                    val dest = if (role == UserRole.CLIENT) Screen.Search.route
                               else Screen.Dashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
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
                    if (role == UserRole.PROVIDER) {
                        // Prestador → configura su perfil primero
                        navController.navigate(Screen.ProviderSetup.route)
                        {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        // Cliente → va directo a la búsqueda
                        navController.navigate(Screen.Search.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Setup del prestador — solo al registrarse
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

        // --- Modo Cliente ---
        clientNavGraph(navController, authViewModel)

        // --- Modo Prestador ---
        providerNavGraph(navController, authViewModel)
    }
}
