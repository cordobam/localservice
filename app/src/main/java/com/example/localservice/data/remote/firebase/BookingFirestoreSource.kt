package com.example.localservice.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.localservice.domain.model.*
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
            val slug = id.replace("-", "").take(8)
            val final = booking.copy(
                id        = id,
                publicSlug = slug,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            collection.document(id).set(final.toMap()).await()
            Result.Success(final)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear la solicitud", e)
        }
    }

    fun getBookingsForClient(clientUid: String): Flow<Result<List<Booking>>> = callbackFlow {
        trySend(Result.Loading)
        val listener = collection
            .whereEqualTo("clientUid", clientUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.Error(err.message ?: "Error")); return@addSnapshotListener }
                trySend(Result.Success(snap?.documents?.mapNotNull { it.toBooking() } ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    fun getBookingsForProvider(providerUid: String): Flow<Result<List<Booking>>> = callbackFlow {
        trySend(Result.Loading)
        val listener = collection
            .whereEqualTo("providerUid", providerUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.Error(err.message ?: "Error")); return@addSnapshotListener }
                trySend(Result.Success(snap?.documents?.mapNotNull { it.toBooking() } ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    // Busca por slug público — para TrackingScreen
    fun getBookingBySlug(slug: String): Flow<Result<Booking>> = callbackFlow {
        trySend(Result.Loading)
        val listener = collection
            .whereEqualTo("publicSlug", slug)
            .limit(1)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.Error(err.message ?: "Error")); return@addSnapshotListener }
                val booking = snap?.documents?.firstOrNull()?.toBooking()
                if (booking != null) trySend(Result.Success(booking))
                else trySend(Result.Error("Trabajo no encontrado"))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        return try {
            collection.document(bookingId).update(
                mapOf("status" to status.name, "updatedAt" to System.currentTimeMillis())
            ).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e.message ?: "Error", e) }
    }

    // Guarda las etapas en el documento del booking
    suspend fun updateStages(bookingId: String, stages: List<Stage>): Result<Unit> {
        return try {
            collection.document(bookingId).update(
                mapOf(
                    "stages"    to stages.map { it.toMap() },
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e.message ?: "Error al guardar etapas", e) }
    }

    // --- Mappers ---

    private fun com.google.firebase.firestore.DocumentSnapshot.toBooking(): Booking? {
        return try {
            val stagesList = (get("stages") as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { map ->
                    Stage(
                        id           = map["id"] as? String ?: "",
                        name         = map["name"] as? String ?: "",
                        status       = StageStatus.valueOf(map["status"] as? String ?: "PENDING"),
                        estimatedDays = (map["estimatedDays"] as? Long)?.toInt() ?: 0,
                        order        = (map["order"] as? Long)?.toInt() ?: 0,
                        completedAt  = map["completedAt"] as? Long
                    )
                }
            } ?: emptyList()

            Booking(
                id             = id,
                providerUid    = getString("providerUid") ?: "",
                providerName   = getString("providerName") ?: "",
                clientUid      = getString("clientUid") ?: "",
                clientName     = getString("clientName") ?: "",
                clientPhone    = getString("clientPhone") ?: "",
                category       = ServiceCategory.valueOf(getString("category") ?: "OTHER"),
                description    = getString("description") ?: "",
                status         = BookingStatus.valueOf(getString("status") ?: "PENDING"),
                budgetAmount   = getLong("budgetAmount")?.toInt() ?: 0,
                budgetApproved = getBoolean("budgetApproved") ?: false,
                publicSlug     = getString("publicSlug") ?: "",
                stages         = stagesList,
                createdAt      = getLong("createdAt") ?: 0L,
                updatedAt      = getLong("updatedAt") ?: 0L
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
        "stages"         to stages.map { it.toMap() },
        "createdAt"      to createdAt,
        "updatedAt"      to updatedAt
    )

    private fun Stage.toMap() = mapOf(
        "id"           to id,
        "name"         to name,
        "status"       to status.name,
        "estimatedDays" to estimatedDays,
        "order"        to order,
        "completedAt"  to completedAt
    )
}
