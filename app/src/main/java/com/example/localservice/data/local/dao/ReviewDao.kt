package com.example.localservice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.localservice.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reviews: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE providerUid = :providerUid ORDER BY createdAt DESC")
    fun getReviewsForProvider(providerUid: String): Flow<List<ReviewEntity>>

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteById(reviewId: String)
}
