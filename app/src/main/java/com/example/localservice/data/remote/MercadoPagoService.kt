package com.example.localservice.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.localservice.domain.model.Booking
import javax.inject.Inject
import javax.inject.Singleton

// Integración con Mercado Pago usando el SDK de cobros por link.
// No requiere backend propio — genera un link de pago que se abre en el browser
// o en la app de Mercado Pago si está instalada.
@Singleton
class MercadoPagoService @Inject constructor() {

    // Genera un link de pago de Mercado Pago para cobrar un servicio.
    // Requiere que el prestador tenga una cuenta de MP y su access token.
    // En producción esto se llama desde una Cloud Function de Firebase
    // para no exponer el access token en el cliente.
    fun generatePaymentLink(
        booking: Booking,
        providerMpAlias: String  // alias de MP del prestador (ej: juan.gomez)
    ): String {
        // Link de pago directo por alias — sin SDK
        // El cliente paga desde su app de MP o desde el browser
        val amount = booking.budgetAmount
        val description = Uri.encode("Servicio: ${booking.category.displayName} - ${booking.clientName}")
        return "https://mpago.la/pagar/$providerMpAlias?amount=$amount&description=$description"
    }

    // Abre el link de pago en el browser/app de MP
    fun openPaymentLink(context: Context, link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // Alternativa: link de cobro simple por alias sin monto fijo
    fun generateSimpleCollectLink(mpAlias: String): String =
        "https://mpago.la/cobrar/$mpAlias"
}
