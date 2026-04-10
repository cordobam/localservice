package com.example.localservice.domain.model

data class Booking(
    val id: String = "",
    val providerUid: String = "",
    val providerName: String = "",
    val clientUid: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val description: String = "",       // qué necesita el cliente
    val status: BookingStatus = BookingStatus.PENDING,
    val budgetAmount: Int = 0,          // presupuesto enviado por el prestador
    val budgetApproved: Boolean = false,
    val publicSlug: String = "",        // link público de seguimiento
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class BookingStatus {
    PENDING,            // cliente solicitó, esperando respuesta
    BUDGET_SENT,        // prestador mandó presupuesto
    BUDGET_APPROVED,    // cliente aprobó presupuesto
    IN_PROGRESS,        // trabajo en curso
    COMPLETED,          // trabajo terminado
    CANCELLED           // cancelado por cualquiera de las partes
}
