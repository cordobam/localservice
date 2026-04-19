package com.example.localservice.di

import com.example.localservice.data.repository.AuthRepositoryImpl
import com.example.localservice.data.repository.BookingRepositoryImpl
import com.example.localservice.data.repository.ChatRepositoryImpl
import com.example.localservice.data.repository.ProviderRepositoryImpl
import com.example.localservice.data.repository.ReviewRepositoryImpl
import com.example.localservice.domain.repository.AuthRepository
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.domain.repository.ChatRepository
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt necesita saber que cuando alguien pide AuthRepository
// tiene que darle AuthRepositoryImpl. Ese "binding" va acá.
// A medida que agregues más repositories, los sumás en este archivo.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindProviderRepository(impl: ProviderRepositoryImpl): ProviderRepository

    @Binds @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds @Singleton abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository  // ← nuevo
}
