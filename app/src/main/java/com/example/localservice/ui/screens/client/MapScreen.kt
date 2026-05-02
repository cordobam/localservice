package com.example.localservice.ui.screens.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.ui.viewmodel.MapViewModel

// Reemplazá con tu API key real
private const val MAPS_API_KEY = "TU_API_KEY_ACA"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNavigateToProviderDetail: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de prestadores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtro por categoría
            if (uiState.providers.isNotEmpty()) {
                CategoryFilterRow(
                    selected = uiState.selectedCategory,
                    onSelected = { viewModel.onCategorySelected(it) }
                )
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.filteredProviders.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay prestadores en esta zona.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Mapa estático con todos los pins
                    StaticMapWithPins(
                        providers = uiState.filteredProviders,
                        selectedProvider = selectedProvider,
                        onProviderSelected = { selectedProvider = it }
                    )

                    // Card del prestador seleccionado
                    selectedProvider?.let { provider ->
                        SelectedProviderCard(
                            provider = provider,
                            onNavigate = { onNavigateToProviderDetail(provider.uid) },
                            onDismiss = { selectedProvider = null }
                        )
                    }

                    // Lista de prestadores debajo del mapa
                    if (selectedProvider == null) {
                        Text(
                            "${uiState.filteredProviders.size} prestadores",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.filteredProviders, key = { it.uid }) { provider ->
                                ProviderMapListItem(
                                    provider = provider,
                                    onClick = { selectedProvider = provider }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticMapWithPins(
    providers: List<Provider>,
    selectedProvider: Provider?,
    onProviderSelected: (Provider) -> Unit
) {
    // Calcula el centro del mapa promediando coordenadas
    val validProviders = providers.filter { it.lat != 0.0 && it.lng != 0.0 }

    if (validProviders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Los prestadores no tienen ubicación configurada.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        return
    }

    val centerLat = validProviders.map { it.lat }.average()
    val centerLng = validProviders.map { it.lng }.average()

    // Construye la URL de la Static Maps API con un pin por prestador
    val baseUrl = "https://maps.googleapis.com/maps/api/staticmap"
    val size = "640x320"
    val scale = "2"
    val zoom = "13"

    // Pins: rojo para todos, azul para el seleccionado
    val markers = validProviders.joinToString("&") { provider ->
        val color = if (provider.uid == selectedProvider?.uid) "blue" else "red"
        val label = provider.category.displayName.first().uppercaseChar()
        "markers=color:$color|label:$label|${provider.lat},${provider.lng}"
    }

    val mapUrl = "$baseUrl?center=$centerLat,$centerLng" +
                 "&zoom=$zoom&size=$size&scale=$scale" +
                 "&$markers" +
                 "&key=$MAPS_API_KEY"

    AsyncImage(
        model = mapUrl,
        contentDescription = "Mapa de prestadores",
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable {
                // Toque en el mapa — selecciona el prestador más cercano al centro
                // En Static API no tenemos coordenadas del toque, así que
                // seleccionamos el primero disponible como demo
                if (selectedProvider == null && validProviders.isNotEmpty()) {
                    onProviderSelected(validProviders.first())
                }
            },
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun CategoryFilterRow(
    selected: ServiceCategory?,
    onSelected: (ServiceCategory?) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("Todos") }
            )
        }
        items(ServiceCategory.mainCategories()) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text("${category.emoji} ${category.displayName}") }
            )
        }
    }
}

@Composable
private fun SelectedProviderCard(
    provider: Provider,
    onNavigate: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    provider.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${provider.category.emoji} ${provider.category.displayName} · ${provider.zone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                if (provider.rating > 0) {
                    Text(
                        "⭐ ${"%.1f".format(provider.rating)} (${provider.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onNavigate,
                    modifier = Modifier.height(36.dp)
                ) { Text("Ver perfil", style = MaterialTheme.typography.labelMedium) }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun ProviderMapListItem(
    provider: Provider,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "${provider.category.emoji} ${provider.category.displayName} · ${provider.zone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (provider.rating > 0) {
                Text(
                    "⭐ ${"%.1f".format(provider.rating)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
