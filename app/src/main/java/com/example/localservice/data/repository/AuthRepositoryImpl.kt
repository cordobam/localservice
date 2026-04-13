package com.example.localservice.data.repository

import com.example.localservice.data.remote.firebase.AuthFirebaseSource
import com.example.localservice.domain.model.User
import com.example.localservice.domain.model.UserRole
import com.example.localservice.domain.repository.AuthRepository
import com.example.localservice.util.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val source: AuthFirebaseSource
) : AuthRepository {

    // Emite el usuario actual al arrancar. Si no hay sesión emite null.
    /*override fun getCurrentUser(): Flow<User?> = flow {
        val firebaseUser = source.currentFirebaseUser()
        if (firebaseUser == null) {
            emit(null)
            return@flow
        }
        val result = source.getUserFromFirestore(firebaseUser.uid)
        emit(if (result is Result.Success) result.data else null)
    }

    override suspend fun login(email: String, password: String): Result<User> =
        source.login(email, password)

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<User> = source.register(name, email, password, phone, role)

    override suspend fun logout() = source.logout()

    override suspend fun isLoggedIn(): Boolean =
        source.currentFirebaseUser() != null */

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser

            if (firebaseUser == null) {
                trySend(null)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = source.getUserFromFirestore(firebaseUser.uid)
                    val user = if (result is Result.Success) result.data else null
                    trySend(user)
                }
            }
        }

        FirebaseAuth.getInstance().addAuthStateListener(listener)

        awaitClose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> =
        source.login(email, password)

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<User> = source.register(name, email, password, phone, role)

    override suspend fun logout() = source.logout()

    override suspend fun isLoggedIn(): Boolean =
        source.currentFirebaseUser() != null
}
