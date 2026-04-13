package com.example.localservice.domain.model

import java.time.LocalDate

data class AgendaSlot(
    val date: LocalDate,
    val bookings: List<Booking> = emptyList()
) {
    val hasWork: Boolean get() = bookings.isNotEmpty()
    val workCount: Int get() = bookings.size
}
