package com.example.localservice.ui.screens.client

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.localservice.ui.screens.provider.PhotoPickerSection
import com.example.localservice.ui.viewmodel.AuthViewModel
import com.example.localservice.ui.viewmodel.MyBookingsViewModel

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

    val authState by authViewModel.uiState.collectAsState()
    val activity = LocalContext.current as ComponentActivity
    val myBookingsViewModel: MyBookingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val myBookingsState by myBookingsViewModel.uiState.collectAsState()

    LaunchedEffect(authState.currentUser) {
        authState.currentUser?.let { myBookingsViewModel.init(it.uid) }
    }

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
                            if (tab is ClientTab.MyBookings && myBookingsState.pendingBudgetCount > 0) {
                                BadgedBox(badge = {
                                    Badge { Text(myBookingsState.pendingBudgetCount.toString()) }
                                }) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            }
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
                    authViewModel = authViewModel,
                    viewModel = myBookingsViewModel
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState.photoUpdated) {
        if (authState.photoUpdated) {
            snackbarHostState.showSnackbar("Foto de perfil actualizada")
            authViewModel.clearPhotoUpdated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.3f))

            PhotoPickerSection(
                photoUri = authState.photoUri,
                onPhotoSelected = authViewModel::onPhotoSelected,
                currentPhotoUrl = user?.photoUrl ?: ""
            )

            Spacer(Modifier.height(16.dp))

            Text(user?.name ?: "", style = MaterialTheme.typography.headlineSmall)
            Text(
                user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            if (authState.photoUri != null) {
                Button(
                    onClick = { authViewModel.updateProfilePhoto() },
                    enabled = !authState.isPhotoUpdating,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (authState.isPhotoUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar foto")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            authState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar sesión") }
            Spacer(Modifier.weight(0.5f))
        }
    }
}
