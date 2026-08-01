package com.example.localservice.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.localservice.domain.model.Review
import com.example.localservice.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("reviews")

    fun getReviewsForProvider(providerUid: String): Flow<Result<List<Review>>> =
        callbackFlow {
            trySend(Result.Loading)

            val listener = collection
                .whereEqualTo("providerUid", providerUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error.message ?: "Error al cargar reseñas"))
                        return@addSnapshotListener
                    }
                    val reviews = snapshot?.documents?.mapNotNull { doc ->
                        doc.toReview()
                    } ?: emptyList()

                    trySend(Result.Success(reviews))
                }

            awaitClose { listener.remove() }
        }

    fun getReviewsByClient(clientUid: String): Flow<Result<List<Review>>> =
        callbackFlow {
            trySend(Result.Loading)

            val listener = collection
                .whereEqualTo("clientUid", clientUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error.message ?: "Error al cargar reseñas"))
                        return@addSnapshotListener
                    }
                    val reviews = snapshot?.documents?.mapNotNull { doc ->
                        doc.toReview()
                    } ?: emptyList()

                    trySend(Result.Success(reviews))
                }

            awaitClose { listener.remove() }
        }

    suspend fun addReview(review: Review): Result<Review> {
        return try {
            val id = UUID.randomUUID().toString()
            collection.document(id)
                .set(mapOf(
                    "providerUid"    to review.providerUid,
                    "clientUid"      to review.clientUid,
                    "clientName"     to review.clientName,
                    "clientPhotoUrl" to review.clientPhotoUrl,
                    "rating"         to review.rating,
                    "comment"        to review.comment,
                    "bookingId"      to review.bookingId,
                    "createdAt"      to System.currentTimeMillis()
                ))
                .await()
            Result.Success(review.copy(id = id))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al guardar reseña", e)
        }
    }

    suspend fun updateReview(review: Review): Result<Unit> {
        return try {
            collection.document(review.id)
                .set(mapOf(
                    "providerUid"    to review.providerUid,
                    "clientUid"      to review.clientUid,
                    "clientName"     to review.clientName,
                    "clientPhotoUrl" to review.clientPhotoUrl,
                    "rating"         to review.rating,
                    "comment"        to review.comment,
                    "bookingId"      to review.bookingId,
                    "createdAt"      to review.createdAt
                ))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar reseña", e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            collection.document(reviewId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al eliminar reseña", e)
        }
    }

    suspend fun getReviewById(reviewId: String): Result<Review?> {
        return try {
            val doc = collection.document(reviewId).get().await()
            if (!doc.exists()) Result.Success(null)
            else Result.Success(doc.toReview())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener reseña", e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReview(): Review? {
        return try {
            Review(
                id             = id,
                providerUid    = getString("providerUid") ?: "",
                clientUid      = getString("clientUid") ?: "",
                clientName     = getString("clientName") ?: "Cliente",
                clientPhotoUrl = getString("clientPhotoUrl") ?: "",
                rating         = (getDouble("rating") ?: 0.0).toFloat(),
                comment        = getString("comment") ?: "",
                bookingId      = getString("bookingId") ?: "",
                createdAt      = getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) { null }
    }
}
