package com.example.localservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.localservice.ui.viewmodel.AuthViewModel
import com.example.localservice.ui.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    providerUid: String,
    providerName: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onSubmitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text("🎉", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                "¿Cómo te fue con $providerName?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                "Tu opinión ayuda a otros vecinos a elegir mejor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Estrellas interactivas
            Text("Calificación", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { viewModel.onRatingChanged(star) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= uiState.rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$star estrellas",
                            tint = if (star <= uiState.rating) MaterialTheme.colorScheme.secondary
                                   else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Label del rating
            if (uiState.rating > 0) {
                Text(
                    text = when (uiState.rating) {
                        1 -> "Muy malo"
                        2 -> "Regular"
                        3 -> "Bien"
                        4 -> "Muy bien"
                        5 -> "Excelente"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.comment,
                onValueChange = { viewModel.onCommentChanged(it) },
                label = { Text("Comentario (opcional)") },
                placeholder = { Text("Contá tu experiencia...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                LaunchedEffect(error) { viewModel.clearError() }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    currentUser?.let {
                        viewModel.submitReview(
                            providerUid = providerUid,
                            clientUid   = it.uid,
                            clientName  = it.name
                        )
                    }
                },
                enabled = uiState.rating > 0 && !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Enviar calificación", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Ahora no")
            }
        }
    }
}
