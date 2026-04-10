package com.example.localservice.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("bookings")

    suspend fun createBooking(booking: Booking): Result<Booking> {
        return try {
            val id = UUID.randomUUID().toString()
            // Slug público: primeras 8 letras del id — suficiente para tracking
            val slug = id.replace("-", "").take(8)
            val finalBooking = booking.copy(
                id         = id,
                publicSlug = slug,
                createdAt  = System.currentTimeMillis(),
                updatedAt  = System.currentTimeMillis()
            )
            collection.document(id).set(finalBooking.toMap()).await()
            Result.Success(finalBooking)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear la solicitud", e)
        }
    }

    fun getBookingsForClient(clientUid: String): Flow<Result<List<Booking>>> =
        callbackFlow {
            trySend(Result.Loading)
            val listener = collection
                .whereEqualTo("clientUid", clientUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error.message ?: "Error"))
                        return@addSnapshotListener
                    }
                    trySend(Result.Success(snapshot?.documents?.mapNotNull { it.toBooking() } ?: emptyList()))
                }
            awaitClose { listener.remove() }
        }

    fun getBookingsForProvider(providerUid: String): Flow<Result<List<Booking>>> =
        callbackFlow {
            trySend(Result.Loading)
            val listener = collection
                .whereEqualTo("providerUid", providerUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error.message ?: "Error"))
                        return@addSnapshotListener
                    }
                    trySend(Result.Success(snapshot?.documents?.mapNotNull { it.toBooking() } ?: emptyList()))
                }
            awaitClose { listener.remove() }
        }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        return try {
            collection.document(bookingId).update(
                mapOf(
                    "status"    to status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar estado", e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toBooking(): Booking? {
        return try {
            Booking(
                id              = id,
                providerUid     = getString("providerUid") ?: "",
                providerName    = getString("providerName") ?: "",
                clientUid       = getString("clientUid") ?: "",
                clientName      = getString("clientName") ?: "",
                clientPhone     = getString("clientPhone") ?: "",
                category        = ServiceCategory.valueOf(getString("category") ?: "OTHER"),
                description     = getString("description") ?: "",
                status          = BookingStatus.valueOf(getString("status") ?: "PENDING"),
                budgetAmount    = getLong("budgetAmount")?.toInt() ?: 0,
                budgetApproved  = getBoolean("budgetApproved") ?: false,
                publicSlug      = getString("publicSlug") ?: "",
                createdAt       = getLong("createdAt") ?: 0L,
                updatedAt       = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            android.util.Log.e("ServiLocal", "Error mapeando booking $id: ${e.message}")
            null
        }
    }

    private fun Booking.toMap() = mapOf(
        "providerUid"    to providerUid,
        "providerName"   to providerName,
        "clientUid"      to clientUid,
        "clientName"     to clientName,
        "clientPhone"    to clientPhone,
        "category"       to category.name,
        "description"    to description,
        "status"         to status.name,
        "budgetAmount"   to budgetAmount,
        "budgetApproved" to budgetApproved,
        "publicSlug"     to publicSlug,
        "createdAt"      to createdAt,
        "updatedAt"      to updatedAt
    )
}
