package com.example.localservice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.localservice.data.local.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bookings: List<BookingEntity>)

    @Query("SELECT * FROM bookings WHERE clientUid = :clientUid ORDER BY createdAt DESC")
    fun getBookingsForClient(clientUid: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE providerUid = :providerUid ORDER BY createdAt DESC")
    fun getBookingsForProvider(providerUid: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE publicSlug = :slug LIMIT 1")
    fun getBookingBySlug(slug: String): Flow<BookingEntity?>

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    suspend fun deleteById(bookingId: String)
}
