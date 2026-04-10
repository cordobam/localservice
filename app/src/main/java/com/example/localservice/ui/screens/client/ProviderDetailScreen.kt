package com.example.localservice.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import com.example.localservice.domain.model.Review
import com.example.localservice.ui.viewmodel.AuthViewModel
import com.example.localservice.ui.viewmodel.ProviderDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    onBack: () -> Unit,
    onBookingCreated: (slug: String) -> Unit,
    authViewModel: AuthViewModel,
    viewModel: ProviderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    // Cuando se crea un booking exitoso, navegamos al tracking
    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess && uiState.bookingSlug.isNotBlank()) {
            onBookingCreated(uiState.bookingSlug)
            viewModel.clearBookingSuccess()
        }
    }

    // Bottom sheet para la solicitud
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.showRequestSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideRequestSheet() },
            sheetState = sheetState
        ) {
            RequestServiceSheet(
                description = uiState.requestDescription,
                isSubmitting = uiState.isSubmitting,
                onDescriptionChanged = { viewModel.onRequestDescriptionChanged(it) },
                onSubmitDirect = {
                    val user = authState.currentUser ?: return@RequestServiceSheet
                    viewModel.submitDirectRequest(
                        clientUid   = user.uid,
                        clientName  = user.name,
                        clientPhone = user.phone
                    )
                },
                onSubmitBudgetRequest = {
                    // Mismo flujo — el prestador decide si manda presupuesto o acepta directo
                    val user = authState.currentUser ?: return@RequestServiceSheet
                    viewModel.submitDirectRequest(
                        clientUid   = user.uid,
                        clientName  = user.name,
                        clientPhone = user.phone
                    )
                },
                onDismiss = { viewModel.hideRequestSheet() }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.provider?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            // Botón fijo abajo para solicitar el servicio
            uiState.provider?.let {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { viewModel.showRequestSheet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp)
                    ) {
                        Text("Solicitar servicio", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            uiState.provider != null -> {
                val provider = uiState.provider!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    // --- Foto de perfil + datos básicos ---
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (provider.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = provider.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(96.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = provider.name.firstOrNull()?.toString() ?: "?",
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(provider.name, style = MaterialTheme.typography.headlineSmall)

                            Text(
                                text = "${provider.category.emoji} ${provider.category.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rating
                            if (provider.reviewCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${"%.1f".format(provider.rating)} · ${provider.reviewCount} reseñas",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Zona
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "${provider.zone} · ${provider.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Precio orientativo
                            if (provider.priceFrom > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Desde $${"%,d".format(provider.priceFrom)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider()
                    }

                    // --- Descripción ---
                    if (provider.description.isNotBlank()) {
                        item {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Sobre mí",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    provider.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }

                    // --- Mapa con zona de trabajo ---
                    if (provider.lat != 0.0 && provider.lng != 0.0) {
                        item {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Zona de trabajo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Mapa estático via URL de Google Maps Static API
                                // No requiere SDK de Maps — solo una imagen
                                val mapUrl = "https://maps.googleapis.com/maps/api/staticmap" +
                                        "?center=${provider.lat},${provider.lng}" +
                                        "&zoom=14&size=600x200&scale=2" +
                                        "&markers=color:red|${provider.lat},${provider.lng}" +
                                        "&key=TU_API_KEY_ACA"
                                AsyncImage(
                                    model = mapUrl,
                                    contentDescription = "Mapa de zona",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Trabaja en ${provider.zone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }

                    // --- Reseñas ---
                    item {
                        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                            Text(
                                "Reseñas (${uiState.reviews.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (uiState.reviews.isEmpty()) {
                        item {
                            Text(
                                "Todavía no hay reseñas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        items(uiState.reviews) { review ->
                            ReviewItem(review = review)
                        }
                    }

                    // Espacio para el botón fijo de abajo
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// --- Componente de reseña individual ---
@Composable
private fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar del cliente
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = review.clientName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(review.clientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                // Estrellas
                Row {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (index < review.rating) MaterialTheme.colorScheme.secondary
                                   else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
        if (review.comment.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
    }
}

// --- Bottom sheet de solicitud ---
@Composable
private fun RequestServiceSheet(
    description: String,
    isSubmitting: Boolean,
    onDescriptionChanged: (String) -> Unit,
    onSubmitDirect: () -> Unit,
    onSubmitBudgetRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text("Solicitar servicio", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Describí qué necesitás y el prestador te va a responder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("¿Qué necesitás?") },
            placeholder = { Text("Ej: Se rompió un caño bajo el lavatorio, hay agua en el piso...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dos opciones de contratación
        Button(
            onClick = onSubmitDirect,
            enabled = description.isNotBlank() && !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isSubmitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Solicitar servicio directo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSubmitBudgetRequest,
            enabled = description.isNotBlank() && !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Pedir presupuesto primero")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}
