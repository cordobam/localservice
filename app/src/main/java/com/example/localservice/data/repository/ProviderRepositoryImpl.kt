package com.example.localservice.data.repository

import com.example.localservice.data.remote.firebase.ProviderFirestoreSource
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.SearchFilter
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepositoryImpl @Inject constructor(
    private val source: ProviderFirestoreSource
) : ProviderRepository {

    override fun searchProviders(filter: SearchFilter): Flow<Result<List<Provider>>> =
        source.searchProviders(filter)

    override suspend fun getProviderById(uid: String): Result<Provider> =
        source.getProviderById(uid)

    override suspend fun updateProviderProfile(provider: Provider): Result<Unit> =
        source.updateProviderProfile(provider)
}
