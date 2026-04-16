package com.example.localservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.model.StageStatus
import com.example.localservice.ui.viewmodel.TrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    slug: String,
    onBack: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(slug) { viewModel.init(slug) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Compartir link público
                    IconButton(onClick = {
                        val link = "https://servilocal.app/p/$slug"
                        clipboard.setText(AnnotatedString(link))
                        snackbarHostState.let {
                            // Mostramos snack en el LaunchedEffect
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir link")
                    }
                }
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.booking == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No encontramos este trabajo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val booking = uiState.booking!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item {
                        // Header del trabajo
                        Text(booking.providerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Text(
                            "${booking.category.emoji} ${booking.category.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            booking.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Text("Progreso del trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(16.dp))
                    }

                    // Timeline de etapas
                    itemsIndexed(uiState.stages) { index, stage ->
                        StageTimelineItem(
                            stage = stage,
                            isLast = index == uiState.stages.lastIndex
                        )
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        // Link público para compartir
                        val link = "https://servilocal.app/p/$slug"
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Link de seguimiento", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text(link, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { clipboard.setText(AnnotatedString(link)) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Copiar link") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageTimelineItem(stage: Stage, isLast: Boolean) {
    val (dotColor, lineColor) = when (stage.status) {
        StageStatus.DONE    -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
        StageStatus.ACTIVE  -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.outlineVariant
        StageStatus.LATE    -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.outlineVariant
        StageStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant to MaterialTheme.colorScheme.outlineVariant
    }

    val statusLabel = when (stage.status) {
        StageStatus.DONE    -> "✓ Completada"
        StageStatus.ACTIVE  -> "→ En curso"
        StageStatus.LATE    -> "⚠ Demorada"
        StageStatus.PENDING -> "Pendiente"
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Columna izquierda: dot + línea
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = dotColor, modifier = Modifier.size(20.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (stage.status == StageStatus.DONE) {
                        Text("✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
            if (!isLast) {
                Spacer(Modifier.width(2.dp))
                Divider(modifier = Modifier.width(2.dp).height(40.dp), color = lineColor)
            }
        }

        Spacer(Modifier.width(12.dp))

        // Contenido
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(stage.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(statusLabel, style = MaterialTheme.typography.bodySmall, color = dotColor)
                if (stage.estimatedDays > 0 && stage.status == StageStatus.PENDING) {
                    Text(
                        "· ${stage.estimatedDays} día${if (stage.estimatedDays > 1) "s" else ""} est.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
