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
                        try {
                            Review(
                                id             = doc.id,
                                providerUid    = doc.getString("providerUid") ?: "",
                                clientUid      = doc.getString("clientUid") ?: "",
                                clientName     = doc.getString("clientName") ?: "Cliente",
                                clientPhotoUrl = doc.getString("clientPhotoUrl") ?: "",
                                rating         = (doc.getDouble("rating") ?: 0.0).toFloat(),
                                comment        = doc.getString("comment") ?: "",
                                createdAt      = doc.getLong("createdAt") ?: 0L
                            )
                        } catch (e: Exception) { null }
                    } ?: emptyList()

                    trySend(Result.Success(reviews))
                }

            awaitClose { listener.remove() }
        }

    suspend fun addReview(review: Review): Result<Unit> {
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
                    "createdAt"      to System.currentTimeMillis()
                ))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al guardar reseña", e)
        }
    }
}
