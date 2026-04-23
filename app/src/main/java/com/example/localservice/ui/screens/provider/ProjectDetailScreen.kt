package com.example.localservice.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.model.StageStatus
import com.example.localservice.ui.components.StatusChip
import com.example.localservice.ui.viewmodel.ProjectDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    onNavigateToStageEditor: (String) -> Unit,
    onNavigateToChat: (bookingId: String, clientName: String) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showCompleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) { viewModel.init(bookingId) }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        val msg = uiState.successMessage ?: uiState.error
        if (msg != null) { snackbar.showSnackbar(msg); viewModel.clearMessages() }
    }

    // Diálogo de confirmación para completar
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("¿Marcar como completado?") },
            text = { Text("El cliente va a recibir una notificación y podrá calificarte.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeWork()
                    showCompleteDialog = false
                }) { Text("Completar") }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.booking?.clientName ?: "Trabajo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Chat con el cliente
                    uiState.booking?.let { booking ->
                        IconButton(onClick = {
                            onNavigateToChat(booking.id, booking.clientName)
                        }) {
                            Icon(Icons.Outlined.Chat, contentDescription = "Chat")
                        }
                    }
                }
            )
        },
        bottomBar = {
            uiState.booking?.let { booking ->
                Surface(shadowElevation = 4.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón editar etapas — siempre disponible si no está completado
                        if (booking.status != BookingStatus.COMPLETED && booking.status != BookingStatus.CANCELLED) {
                            OutlinedButton(
                                onClick = { onNavigateToStageEditor(bookingId) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) { Text("Editar etapas") }

                            // Botón completar — solo cuando todas las etapas están DONE
                            val allDone = uiState.stages.isNotEmpty() &&
                                          uiState.stages.all { it.status == StageStatus.DONE }
                            Button(
                                onClick = { showCompleteDialog = true },
                                enabled = allDone && !uiState.isActioning,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                if (uiState.isActioning) CircularProgressIndicator(
                                    Modifier.size(20.dp), strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                else Text(
                                    if (allDone) "Marcar como completado"
                                    else "Completá todas las etapas primero",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.booking == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Trabajo no encontrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val booking = uiState.booking!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Info del trabajo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${booking.category.emoji} ${booking.category.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                    StatusChip(status = booking.status)
                                }
                                Text(booking.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (booking.budgetAmount > 0) {
                                    Text(
                                        "Presupuesto: $${"%.0f".format(booking.budgetAmount.toDouble())}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Etapas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    }

                    if (uiState.stages.isEmpty()) {
                        item {
                            OutlinedButton(
                                onClick = { onNavigateToStageEditor(bookingId) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Agregar etapas del trabajo") }
                        }
                    } else {
                        items(uiState.stages.size) { index ->
                            val stage = uiState.stages[index]
                            val statusColor = when (stage.status) {
                                StageStatus.DONE    -> MaterialTheme.colorScheme.secondary
                                StageStatus.ACTIVE  -> MaterialTheme.colorScheme.primary
                                StageStatus.LATE    -> MaterialTheme.colorScheme.error
                                StageStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant
                            }
                            val statusLabel = when (stage.status) {
                                StageStatus.DONE    -> "✓ Completada"
                                StageStatus.ACTIVE  -> "→ En curso"
                                StageStatus.LATE    -> "⚠ Demorada"
                                StageStatus.PENDING -> "Pendiente"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = MaterialTheme.shapes.small, color = statusColor, modifier = Modifier.size(20.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.surface)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(stage.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(statusLabel, style = MaterialTheme.typography.bodySmall, color = statusColor)
                                }
                            }
                            if (index < uiState.stages.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 32.dp))
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
