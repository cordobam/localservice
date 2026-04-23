package com.example.localservice.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localservice.data.remote.MercadoPagoService
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus

// Componente que se agrega en ClientBookingCard cuando el presupuesto está aprobado.
// El cliente puede pagar via Mercado Pago directo desde la app.
@Composable
fun PaymentSection(
    booking: Booking,
    providerMpAlias: String,   // el prestador lo configura en su perfil
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mpService = MercadoPagoService()

    if (booking.status == BookingStatus.BUDGET_APPROVED && booking.budgetAmount > 0) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Pago pendiente",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${"%.0f".format(booking.budgetAmount.toDouble())} ARS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val link = mpService.generatePaymentLink(booking, providerMpAlias)
                        mpService.openPaymentLink(context, link)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Pagar con Mercado Pago")
                }
            }
        }
    }
}

// Para agregar el alias de MP en el perfil del prestador,
// agregá este campo en Provider.kt:
// val mpAlias: String = ""
//
// Y en ProviderProfileScreen agregá el campo:
// OutlinedTextField(
//     value = uiState.mpAlias,
//     onValueChange = { viewModel.onMpAliasChanged(it) },
//     label = { Text("Alias de Mercado Pago") },
//     placeholder = { Text("Ej: juan.gomez") },
//     singleLine = true,
//     modifier = Modifier.fillMaxWidth()
// )
