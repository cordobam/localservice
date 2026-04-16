package com.example.localservice.domain.repository

import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.model.Stage
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(booking: Booking): Result<Booking>
    fun getBookingsForClient(clientUid: String): Flow<Result<List<Booking>>>
    fun getBookingsForProvider(providerUid: String): Flow<Result<List<Booking>>>
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit>
    suspend fun updateStages(bookingId: String, stages: List<Stage>): Result<Unit>   // ← nuevo
    fun getBookingBySlug(slug: String): Flow<Result<Booking>>                        // ← nuevo
}
