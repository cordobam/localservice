package com.example.localservice.ui.screens.client

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.localservice.ui.viewmodel.AuthViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth

// Rutas internas del tab bar del cliente
private sealed class ClientTab(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Search     : ClientTab("tab_search",    "Buscar",    Icons.Filled.Search, Icons.Outlined.Search)
    object MyBookings : ClientTab("tab_bookings",  "Mis pedidos", Icons.Filled.List, Icons.Outlined.List)
    object Profile    : ClientTab("tab_profile",   "Perfil",    Icons.Filled.Person, Icons.Outlined.Person)
}

private val tabs = listOf(ClientTab.Search, ClientTab.MyBookings, ClientTab.Profile)

@Composable
fun ClientMainScreen(
    onLogout: () -> Unit,
    onNavigateToProviderDetail: (String) -> Unit,
    onNavigateToTracking: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    val tabNavController = rememberNavController()
    val navBackStack by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(ClientTab.Search.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = ClientTab.Search.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ClientTab.Search.route) {
                SearchScreen(
                    onNavigateToProviderDetail = onNavigateToProviderDetail,
                    onLogout = onLogout
                )
            }

            composable(ClientTab.MyBookings.route) {
                MyBookingsScreen(
                    onBack = { tabNavController.popBackStack() },
                    onNavigateToTracking = onNavigateToTracking,
                    authViewModel = authViewModel
                )
            }

            composable(ClientTab.Profile.route) {
                ClientProfileScreen(
                    onLogout = onLogout,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

// Perfil del cliente — simple por ahora
@Composable
private fun ClientProfileScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.currentUser

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        Text(user?.name ?: "", style = MaterialTheme.typography.headlineSmall)
        Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.weight(1f))
        OutlinedButton(onClick = onLogout, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
        androidx.compose.foundation.layout.Spacer(Modifier.weight(0.5f))
    }
}
