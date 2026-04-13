package com.example.localservice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingCard(
    booking: Booking,
    showActions: Boolean = false,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onSendBudget: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "AR"))
    val dateStr = dateFormat.format(Date(booking.createdAt))

    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header: categoría + estado + fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        booking.category.emoji,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        booking.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                StatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del cliente
            Text(
                text = booking.clientName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            // Descripción del pedido
            Text(
                text = booking.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Fecha
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            // Acciones — solo para pedidos pendientes
            if (showActions && booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rechazar
                    OutlinedButton(
                        onClick = { onReject?.invoke() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Rechazar", style = MaterialTheme.typography.labelMedium)
                    }

                    // Presupuesto
                    OutlinedButton(
                        onClick = { onSendBudget?.invoke() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Presupuesto", style = MaterialTheme.typography.labelMedium)
                    }

                    // Aceptar directo
                    Button(
                        onClick = { onAccept?.invoke() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceptar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val (label, containerColor, contentColor) = when (status) {
        BookingStatus.PENDING         -> Triple("Pendiente",   MaterialTheme.colorScheme.errorContainer,     MaterialTheme.colorScheme.onErrorContainer)
        BookingStatus.BUDGET_SENT     -> Triple("Presupuesto", MaterialTheme.colorScheme.tertiaryContainer,  MaterialTheme.colorScheme.onTertiaryContainer)
        BookingStatus.BUDGET_APPROVED -> Triple("Aprobado",    MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        BookingStatus.IN_PROGRESS     -> Triple("En curso",    MaterialTheme.colorScheme.primaryContainer,   MaterialTheme.colorScheme.onPrimaryContainer)
        BookingStatus.COMPLETED       -> Triple("Completado",  MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        BookingStatus.CANCELLED       -> Triple("Cancelado",   MaterialTheme.colorScheme.surfaceVariant,     MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
