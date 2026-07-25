package com.example.localservice.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.localservice.data.local.dao.BookingDao
import com.example.localservice.data.local.dao.ChatMessageDao
import com.example.localservice.data.local.dao.ProviderDao
import com.example.localservice.data.local.dao.ReviewDao
import com.example.localservice.data.local.dao.UserDao
import com.example.localservice.data.local.entity.BookingEntity
import com.example.localservice.data.local.entity.ChatMessageEntity
import com.example.localservice.data.local.entity.ProviderEntity
import com.example.localservice.data.local.entity.ReviewEntity
import com.example.localservice.data.local.entity.UserEntity

@Database(
    entities = [
        BookingEntity::class,
        ProviderEntity::class,
        UserEntity::class,
        ReviewEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun providerDao(): ProviderDao
    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao
    abstract fun chatMessageDao(): ChatMessageDao
}
