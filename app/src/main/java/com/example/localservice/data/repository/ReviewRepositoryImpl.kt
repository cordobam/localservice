package com.example.localservice.data.repository

import com.example.localservice.data.remote.firebase.ReviewFirestoreSource
import com.example.localservice.domain.model.Review
import com.example.localservice.domain.repository.ReviewRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val source: ReviewFirestoreSource
) : ReviewRepository {
    override fun getReviewsForProvider(providerUid: String) =
        source.getReviewsForProvider(providerUid)

    override suspend fun addReview(review: Review) =
        source.addReview(review)
}
