package com.example.localservice.domain.repository

import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.SearchFilter
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow

interface ProviderRepository {

    // Búsqueda con filtros — devuelve Flow para actualizaciones en tiempo real
    fun searchProviders(filter: SearchFilter): Flow<Result<List<Provider>>>

    // Perfil completo de un prestador
    suspend fun getProviderById(uid: String): Result<Provider>

    // El prestador actualiza su propio perfil
    suspend fun updateProviderProfile(provider: Provider): Result<Unit>
}
