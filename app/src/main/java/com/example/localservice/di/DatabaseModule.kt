package com.example.localservice.di

import android.content.Context
import androidx.room.Room
import com.example.localservice.data.local.AppDatabase
import com.example.localservice.data.local.dao.BookingDao
import com.example.localservice.data.local.dao.ChatMessageDao
import com.example.localservice.data.local.dao.ProviderDao
import com.example.localservice.data.local.dao.ReviewDao
import com.example.localservice.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "servilocal.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBookingDao(db: AppDatabase): BookingDao = db.bookingDao()
    @Provides fun provideProviderDao(db: AppDatabase): ProviderDao = db.providerDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideReviewDao(db: AppDatabase): ReviewDao = db.reviewDao()
    @Provides fun provideChatMessageDao(db: AppDatabase): ChatMessageDao = db.chatMessageDao()
}
