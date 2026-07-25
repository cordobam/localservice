package com.example.localservice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.localservice.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(providers: List<ProviderEntity>)

    @Query("SELECT * FROM providers WHERE isAvailable = 1 ORDER BY rating DESC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE uid = :uid")
    suspend fun getProviderById(uid: String): ProviderEntity?

    @Query("DELETE FROM providers")
    suspend fun deleteAll()
}
