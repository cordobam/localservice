package com.example.localservice.data.remote.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.SearchFilter
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("providers")

    // Búsqueda en tiempo real con filtros.
    // Firestore no soporta filtros compuestos sin índice — por eso
    // filtramos por categoría en Firestore y por zona en memoria.
    // Cuando el volumen crezca, creás índices compuestos en la consola.
    fun searchProviders(filter: SearchFilter): Flow<Result<List<Provider>>> =
        callbackFlow {
            trySend(Result.Loading)

            var query: Query = collection
                .whereEqualTo("isAvailable", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(50)

            // Filtro por categoría en Firestore
            filter.category?.let { category ->
                query = query.whereEqualTo("category", category.name)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIRESTORE_ERROR", "Error completo", error)
                    trySend(Result.Error(error.message ?: "Error al buscar"))
                    return@addSnapshotListener
                }

                var providers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toProvider()
                } ?: emptyList()

                // Filtro por zona en memoria (más flexible que Firestore)
                filter.zone?.let { zone ->
                    providers = providers.filter {
                        it.zone.contains(zone, ignoreCase = true)
                    }
                }

                // Filtro por distancia si el usuario compartió ubicación
                if (filter.userLat != null && filter.userLng != null) {
                    providers = providers.filter { provider ->
                        val distance = calculateDistanceKm(
                            filter.userLat, filter.userLng,
                            provider.lat, provider.lng
                        )
                        distance <= filter.radiusKm
                    }
                    // Ordena por cercanía cuando hay coordenadas
                    providers = providers.sortedBy { provider ->
                        calculateDistanceKm(
                            filter.userLat, filter.userLng,
                            provider.lat, provider.lng
                        )
                    }
                }

                trySend(Result.Success(providers))
            }

            awaitClose { listener.remove() }
        }

    suspend fun getProviderById(uid: String): Result<Provider> {
        return try {
            val doc = collection.document(uid).get().await()
            val provider = doc.toProvider()
                ?: return Result.Error("Prestador no encontrado")
            Result.Success(provider)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener prestador", e)
        }
    }

    suspend fun updateProviderProfile(provider: Provider): Result<Unit> {
        return try {
            collection.document(provider.uid)
                .set(provider.toMap())
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar perfil", e)
        }
    }

    // --- Helpers privados ---

    private fun com.google.firebase.firestore.DocumentSnapshot.toProvider(): Provider? {
        return try {
            Provider(
                uid         = id,
                name        = getString("name") ?: "",
                photoUrl    = getString("photoUrl") ?: "",
                category    = ServiceCategory.valueOf(getString("category") ?: "OTHER"),
                description = getString("description") ?: "",
                zone        = getString("zone") ?: "",
                city        = getString("city") ?: "",
                lat         = getDouble("lat") ?: 0.0,
                lng         = getDouble("lng") ?: 0.0,
                rating      = (getDouble("rating") ?: 0.0).toFloat(),
                reviewCount = getLong("reviewCount")?.toInt() ?: 0,
                priceFrom   = getLong("priceFrom")?.toInt() ?: 0,
                isAvailable = getBoolean("isAvailable") ?: true,
                createdAt   = getLong("createdAt") ?: 0L,
                mpAlias     = getString("mpAlias") ?: ""
            )
        } catch (e: Exception) {
            android.util.Log.e("ServiLocal", "Error mapeando provider ${id}: ${e.message}")
            null
        }
    }

    private fun Provider.toMap() = mapOf(
        "name"        to name,
        "photoUrl"    to photoUrl,
        "category"    to category.name,
        "description" to description,
        "zone"        to zone,
        "city"        to city,
        "lat"         to lat,
        "lng"         to lng,
        "rating"      to rating,
        "reviewCount" to reviewCount,
        "priceFrom"   to priceFrom,
        "isAvailable" to isAvailable,
        "createdAt"   to createdAt,
        "mpAlias"     to mpAlias
    )

    // Fórmula de Haversine para calcular distancia entre dos coordenadas en km
    private fun calculateDistanceKm(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        if (lat2 == 0.0 && lng2 == 0.0) return Double.MAX_VALUE
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}
