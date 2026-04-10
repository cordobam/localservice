package com.example.localservice.data.repository

import com.example.localservice.data.remote.firebase.BookingFirestoreSource
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val source: BookingFirestoreSource
) : BookingRepository {
    override suspend fun createBooking(booking: Booking) = source.createBooking(booking)
    override fun getBookingsForClient(clientUid: String) = source.getBookingsForClient(clientUid)
    override fun getBookingsForProvider(providerUid: String) = source.getBookingsForProvider(providerUid)
    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) =
        source.updateBookingStatus(bookingId, status)
}
