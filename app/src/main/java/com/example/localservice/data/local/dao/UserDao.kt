package com.example.localservice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.localservice.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): UserEntity?

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteById(uid: String)
}
