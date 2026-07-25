package com.example.localservice.data.repository

import com.example.localservice.data.local.dao.ProviderDao
import com.example.localservice.data.local.entity.ProviderEntity
import com.example.localservice.data.remote.firebase.ProviderFirestoreSource
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.SearchFilter
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.domain.repository.ProviderRepository
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
class ProviderRepositoryImpl @Inject constructor(
    private val source: ProviderFirestoreSource,
    private val providerDao: ProviderDao
) : ProviderRepository {

    override fun searchProviders(filter: SearchFilter): Flow<Result<List<Provider>>> = flow {
        val roomFlow = providerDao.getAllProviders().map { entities ->
            val providers = entities.map { it.toDomain() }
            val filtered = providers.filter { p ->
                (filter.category == null || p.category == filter.category) &&
                (filter.zone == null || p.zone.contains(filter.zone, ignoreCase = true))
            }
            Result.Success(filtered) as Result<List<Provider>>
        }
        coroutineScope {
            launch {
                source.searchProviders(filter).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) {
                            providerDao.deleteAll()
                            providerDao.upsertAll(result.data.map { it.toEntity() })
                        }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override suspend fun getProviderById(uid: String): Result<Provider> {
        val remote = source.getProviderById(uid)
        if (remote is Result.Success) {
            withContext(Dispatchers.IO) { providerDao.upsert(remote.data.toEntity()) }
            return remote
        }
        val cached = withContext(Dispatchers.IO) { providerDao.getProviderById(uid) }
        return if (cached != null) Result.Success(cached.toDomain())
        else remote
    }

    override suspend fun updateProviderProfile(provider: Provider): Result<Unit> {
        val result = source.updateProviderProfile(provider)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { providerDao.upsert(provider.toEntity()) }
        }
        return result
    }

    private fun Provider.toEntity() = ProviderEntity(
        uid = uid,
        name = name,
        photoUrl = photoUrl,
        category = category.name,
        description = description,
        zone = zone,
        city = city,
        lat = lat,
        lng = lng,
        rating = rating,
        reviewCount = reviewCount,
        priceFrom = priceFrom,
        isAvailable = isAvailable,
        createdAt = createdAt,
        mpAlias = mpAlias
    )

    private fun ProviderEntity.toDomain() = Provider(
        uid = uid,
        name = name,
        photoUrl = photoUrl,
        category = ServiceCategory.valueOf(category),
        description = description,
        zone = zone,
        city = city,
        lat = lat,
        lng = lng,
        rating = rating,
        reviewCount = reviewCount,
        priceFrom = priceFrom,
        isAvailable = isAvailable,
        createdAt = createdAt,
        mpAlias = mpAlias
    )
}
