package com.example.localservice.ui.screens.provider

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.localservice.ui.components.BookingCard
import com.example.localservice.ui.components.MiniCalendar
import com.example.localservice.ui.viewmodel.AuthViewModel
import com.example.localservice.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToRequests: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()


    val currentUser = authState.currentUser
    LaunchedEffect(currentUser) {
        if (currentUser != null && uiState.providerUid.isBlank()) {
            viewModel.init(providerUid = currentUser.uid, providerName = currentUser.name)
        }
    }

    // Snackbar para mensajes de éxito y error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.error) {
        val msg = uiState.successMessage ?: uiState.error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    // Bottom sheet de presupuesto
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (uiState.showBudgetSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeBudgetSheet() },
            sheetState = sheetState
        ) {
            BudgetSheet(
                amount = uiState.budgetAmount,
                note = uiState.budgetNote,
                isSubmitting = uiState.isActioning,
                onAmountChanged = { viewModel.onBudgetAmountChanged(it) },
                onNoteChanged = { viewModel.onBudgetNoteChanged(it) },
                onSend = { viewModel.sendBudget() },
                onDismiss = { viewModel.closeBudgetSheet() }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ServiLocal Pro") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    TextButton(onClick = onLogout) { Text("Salir") }
                }
            )
        }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // --- Métricas rápidas ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "Pendientes",
                        value = uiState.pendingCount.toString(),
                        modifier = Modifier.weight(1f),
                        highlighted = uiState.pendingCount > 0
                    )
                    MetricCard(
                        label = "En curso",
                        value = uiState.activeCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Rating",
                        value = if (uiState.averageRating > 0)
                            "${"%.1f".format(uiState.averageRating)} ⭐"
                        else "—",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- Pedidos nuevos ---
            item {
                SectionHeader(
                    title = "Pedidos nuevos",
                    count = uiState.pendingCount
                )
            }

            if (uiState.pendingBookings.isEmpty()) {
                item {
                    EmptySection("No hay pedidos nuevos por ahora.")
                }
            } else {
                items(
                    items = uiState.pendingBookings,
                    key = { it.id }
                ) { booking ->
                    BookingCard(
                        booking = booking,
                        showActions = true,
                        onAccept = { viewModel.acceptBooking(booking.id) },
                        onReject = { viewModel.rejectBooking(booking.id) },
                        onSendBudget = { viewModel.openBudgetSheet(booking.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider() }

            // --- Agenda ---
            item {
                SectionHeader(title = "Agenda", count = null)
                MiniCalendar(
                    selectedDate = uiState.selectedDate,
                    datesWithWork = uiState.agendaByDate.keys,
                    onDateSelected = { viewModel.onDateSelected(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Trabajos del día seleccionado
            if (uiState.selectedDateBookings.isEmpty()) {
                item {
                    EmptySection("Sin trabajos para este día.")
                }
            } else {
                items(
                    items = uiState.selectedDateBookings,
                    key = { "agenda_${it.id}" }
                ) { booking ->
                    BookingCard(
                        booking = booking,
                        onClick = { onNavigateToProject(booking.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider() }

            // --- Trabajos en curso ---
            item {
                SectionHeader(title = "Trabajos en curso", count = uiState.activeCount)
            }

            if (uiState.activeBookings.isEmpty()) {
                item { EmptySection("No hay trabajos activos.") }
            } else {
                items(
                    items = uiState.activeBookings,
                    key = { "active_${it.id}" }
                ) { booking ->
                    BookingCard(
                        booking = booking,
                        onClick = { onNavigateToProject(booking.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider() }

            // --- Últimas reseñas ---
            item {
                SectionHeader(title = "Últimas reseñas", count = uiState.reviews.size)
            }

            if (uiState.reviews.isEmpty()) {
                item { EmptySection("Todavía no tenés reseñas.") }
            } else {
                items(uiState.reviews.take(5), key = { "review_${it.id}" }) { review ->
                    ListItem(
                        headlineContent = {
                            Text(review.clientName, fontWeight = FontWeight.Medium)
                        },
                        supportingContent = {
                            Column {
                                Row {
                                    repeat(5) { i ->
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (i < review.rating)
                                                MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                                if (review.comment.isNotBlank()) {
                                    Text(
                                        review.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// --- Componentes internos ---

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (highlighted) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlighted) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (count != null && count > 0) {
            Badge { Text(count.toString()) }
        }
    }
}

@Composable
private fun EmptySection(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// --- Bottom sheet de presupuesto ---
@Composable
private fun BudgetSheet(
    amount: String,
    note: String,
    isSubmitting: Boolean,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text("Enviar presupuesto", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "El cliente va a poder aceptarlo o rechazarlo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChanged,
            label = { Text("Monto ($)") },
            placeholder = { Text("Ej: 15000") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChanged,
            label = { Text("Nota para el cliente (opcional)") },
            placeholder = { Text("Ej: Incluye materiales y mano de obra...") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSend,
            enabled = amount.isNotBlank() && !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isSubmitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Enviar presupuesto")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}
