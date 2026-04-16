package com.example.localservice.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.model.StageStatus
import com.example.localservice.ui.viewmodel.StageEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageEditorScreen(
    bookingId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: StageEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingId) { viewModel.init(bookingId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etapas del trabajo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveStages() },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Guardar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addStage() }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar etapa")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Definí las etapas del trabajo. El cliente las va a ver en tiempo real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            itemsIndexed(uiState.stages) { index, stage ->
                StageEditorCard(
                    stage = stage,
                    index = index,
                    onNameChanged = { viewModel.updateStageName(index, it) },
                    onDaysChanged = { viewModel.updateStageDays(index, it) },
                    onStatusChanged = { viewModel.updateStageStatus(index, it) },
                    onDelete = { viewModel.removeStage(index) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StageEditorCard(
    stage: Stage,
    index: Int,
    onNameChanged: (String) -> Unit,
    onDaysChanged: (String) -> Unit,
    onStatusChanged: (StageStatus) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = stage.name,
                    onValueChange = onNameChanged,
                    label = { Text("Nombre de la etapa") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = if (stage.estimatedDays > 0) stage.estimatedDays.toString() else "",
                    onValueChange = onDaysChanged,
                    label = { Text("Días estimados") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Estado actual de la etapa
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = stage.status.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        StageStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label()) },
                                onClick = {
                                    onStatusChanged(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun StageStatus.label() = when (this) {
    StageStatus.PENDING -> "Pendiente"
    StageStatus.ACTIVE  -> "En curso"
    StageStatus.DONE    -> "Completada"
    StageStatus.LATE    -> "Demorada"
}
