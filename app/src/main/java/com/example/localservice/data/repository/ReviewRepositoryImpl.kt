package com.example.localservice.data.repository

import com.example.localservice.data.local.dao.ReviewDao
import com.example.localservice.data.local.entity.ReviewEntity
import com.example.localservice.data.remote.firebase.ReviewFirestoreSource
import com.example.localservice.domain.model.Review
import com.example.localservice.domain.repository.ReviewRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val source: ReviewFirestoreSource,
    private val reviewDao: ReviewDao
) : ReviewRepository {

    override fun getReviewsForProvider(providerUid: String): Flow<Result<List<Review>>> = flow {
        val roomFlow = reviewDao.getReviewsForProvider(providerUid).map { entities ->
            Result.Success(entities.map { it.toDomain() }) as Result<List<Review>>
        }
        coroutineScope {
            launch {
                source.getReviewsForProvider(providerUid).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) {
                            reviewDao.deleteByProviderUid(providerUid)
                            reviewDao.upsertAll(result.data.map { it.toEntity() })
                        }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override fun getReviewsByClient(clientUid: String): Flow<Result<List<Review>>> = flow {
        val roomFlow = reviewDao.getReviewsByClient(clientUid).map { entities ->
            Result.Success(entities.map { it.toDomain() }) as Result<List<Review>>
        }
        coroutineScope {
            launch {
                source.getReviewsByClient(clientUid).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) {
                            reviewDao.deleteByClientUid(clientUid)
                            reviewDao.upsertAll(result.data.map { it.toEntity() })
                        }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override suspend fun getReviewById(reviewId: String): Result<Review?> {
        val remote = source.getReviewById(reviewId)
        if (remote is Result.Success) {
            remote.data?.let {
                withContext(Dispatchers.IO) { reviewDao.upsert(it.toEntity()) }
            }
            return remote
        }
        val cached = withContext(Dispatchers.IO) { reviewDao.getReviewById(reviewId) }
        return if (cached != null) Result.Success(cached.toDomain())
        else remote
    }

    override suspend fun addReview(review: Review): Result<Review> {
        val result = source.addReview(review)
        if (result is Result.Success) {
            result.data?.let {
                withContext(Dispatchers.IO) { reviewDao.upsert(it.toEntity()) }
            }
        }
        return result
    }

    override suspend fun updateReview(review: Review): Result<Unit> {
        val result = source.updateReview(review)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { reviewDao.upsert(review.toEntity()) }
        }
        return result
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        val result = source.deleteReview(reviewId)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { reviewDao.deleteById(reviewId) }
        }
        return result
    }

    private fun Review.toEntity() = ReviewEntity(
        id = id,
        providerUid = providerUid,
        clientUid = clientUid,
        clientName = clientName,
        clientPhotoUrl = clientPhotoUrl,
        rating = rating,
        comment = comment,
        bookingId = bookingId,
        createdAt = createdAt
    )

    private fun ReviewEntity.toDomain() = Review(
        id = id,
        providerUid = providerUid,
        clientUid = clientUid,
        clientName = clientName,
        clientPhotoUrl = clientPhotoUrl,
        rating = rating,
        comment = comment,
        bookingId = bookingId,
        createdAt = createdAt
    )
}
