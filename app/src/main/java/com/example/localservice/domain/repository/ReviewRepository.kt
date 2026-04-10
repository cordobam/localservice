package com.example.localservice.domain.repository

import com.example.localservice.domain.model.Review
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewsForProvider(providerUid: String): Flow<Result<List<Review>>>
    suspend fun addReview(review: Review): Result<Unit>
}
