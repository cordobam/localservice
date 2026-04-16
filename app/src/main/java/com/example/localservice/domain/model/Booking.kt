package com.example.localservice.domain.model

data class Booking(
    val id: String = "",
    val providerUid: String = "",
    val providerName: String = "",
    val clientUid: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val description: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val budgetAmount: Int = 0,
    val budgetApproved: Boolean = false,
    val publicSlug: String = "",
    val stages: List<Stage> = emptyList(),   // ← nuevo
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class BookingStatus {
    PENDING, BUDGET_SENT, BUDGET_APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
}
