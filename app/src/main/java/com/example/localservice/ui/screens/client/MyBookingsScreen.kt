package com.example.localservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.ui.components.BookingCard
import com.example.localservice.ui.components.StatusChip
import com.example.localservice.ui.viewmodel.AuthViewModel
import com.example.localservice.ui.viewmodel.MyBookingsViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    authViewModel: AuthViewModel,
    viewModel: MyBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    // Inicializa con el uid del cliente logueado
    LaunchedEffect(authState.currentUser) {
        authState.currentUser?.let { viewModel.init(it.uid) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.error) {
        val msg = uiState.successMessage ?: uiState.error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis pedidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.activeBookings.isEmpty() && uiState.pastBookings.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Todavía no hiciste ningún pedido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {

                    // --- Pedidos activos ---
                    if (uiState.activeBookings.isNotEmpty()) {
                        item {
                            Text(
                                "En curso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp,
                                    top = 16.dp, bottom = 8.dp
                                )
                            )
                        }

                        items(
                            items = uiState.activeBookings,
                            key = { it.id }
                        ) { booking ->
                            ClientBookingCard(
                                booking = booking,
                                isActioning = uiState.isActioning,
                                onApproveBudget = { viewModel.approveBudget(booking.id) },
                                onRejectBudget = { viewModel.rejectBudget(booking.id) },
                                onTrack = { onNavigateToTracking(booking.publicSlug) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // --- Pedidos pasados ---
                    if (uiState.pastBookings.isNotEmpty()) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                "Historial",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp,
                                    top = 8.dp, bottom = 8.dp
                                )
                            )
                        }

                        items(
                            items = uiState.pastBookings,
                            key = { "past_${it.id}" }
                        ) { booking ->
                            BookingCard(
                                booking = booking,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Card especializada para el cliente — muestra acciones según el estado
@Composable
private fun ClientBookingCard(
    booking: Booking,
    isActioning: Boolean,
    onApproveBudget: () -> Unit,
    onRejectBudget: () -> Unit,
    onTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${booking.category.emoji} ${booking.providerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                StatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = booking.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            // Mensaje de estado amigable
            Spacer(modifier = Modifier.height(10.dp))
            StatusMessage(booking = booking)

            // Acciones según estado
            when (booking.status) {

                // Prestador mandó presupuesto → cliente puede aprobar o rechazar
                BookingStatus.BUDGET_SENT -> {
                    if (booking.budgetAmount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Presupuesto: $${
                                    NumberFormat.getNumberInstance(Locale("es", "AR"))
                                        .format(booking.budgetAmount)
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onRejectBudget,
                            enabled = !isActioning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Rechazar") }

                        Button(
                            onClick = onApproveBudget,
                            enabled = !isActioning,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isActioning) CircularProgressIndicator(
                                Modifier.size(16.dp), strokeWidth = 2.dp
                            )
                            else Text("Aprobar")
                        }
                    }
                }

                // Trabajo en curso → puede ver el seguimiento
                BookingStatus.IN_PROGRESS,
                BookingStatus.BUDGET_APPROVED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onTrack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver seguimiento del trabajo")
                    }
                }

                else -> Unit
            }
        }
    }
}

// Texto amigable según el estado del pedido
@Composable
private fun StatusMessage(booking: Booking) {
    val (icon, message) = when (booking.status) {
        BookingStatus.PENDING ->
            "⏳" to "Tu pedido fue enviado. Esperando respuesta del prestador."
        BookingStatus.BUDGET_SENT ->
            "💬" to "El prestador te mandó un presupuesto. Revisalo y decidí."
        BookingStatus.BUDGET_APPROVED ->
            "✅" to "Aprobaste el presupuesto. El trabajo está confirmado."
        BookingStatus.IN_PROGRESS ->
            "🔧" to "El trabajo está en curso. Podés ver las etapas en tiempo real."
        BookingStatus.COMPLETED ->
            "🎉" to "Trabajo completado."
        BookingStatus.CANCELLED ->
            "❌" to "Este pedido fue cancelado."
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(icon, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
