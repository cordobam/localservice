package com.example.localservice.ui.screens.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.localservice.ui.viewmodel.AuthViewModel

private sealed class ClientTab(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Search     : ClientTab("tab_search",   "Buscar",     Icons.Filled.Search, Icons.Outlined.Search)
    object Map        : ClientTab("tab_map",      "Mapa",       Icons.Filled.Map,    Icons.Outlined.Map)
    object MyBookings : ClientTab("tab_bookings", "Mis pedidos",Icons.Filled.List,   Icons.Outlined.List)
    object Profile    : ClientTab("tab_profile",  "Perfil",     Icons.Filled.Person, Icons.Outlined.Person)
}

private val tabs = listOf(
    ClientTab.Search,
    ClientTab.Map,
    ClientTab.MyBookings,
    ClientTab.Profile
)

@Composable
fun ClientMainScreen(
    onLogout: () -> Unit,
    onNavigateToProviderDetail: (String) -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToReview: (String, String) -> Unit,
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

            // Tab del mapa
            composable(ClientTab.Map.route) {
                MapScreen(
                    onBack = { tabNavController.popBackStack() },
                    onNavigateToProviderDetail = onNavigateToProviderDetail
                )
            }

            composable(ClientTab.MyBookings.route) {
                MyBookingsScreen(
                    onBack = { tabNavController.popBackStack() },
                    onNavigateToTracking = onNavigateToTracking,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToReview = onNavigateToReview,
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

@Composable
private fun ClientProfileScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text(user?.name ?: "", style = MaterialTheme.typography.headlineSmall)
        Text(
            user?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Cerrar sesión") }
        Spacer(Modifier.weight(0.5f))
    }
}
