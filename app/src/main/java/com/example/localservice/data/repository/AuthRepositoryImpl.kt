package com.example.localservice.data.repository

import com.example.localservice.data.local.dao.UserDao
import com.example.localservice.data.local.entity.UserEntity
import com.example.localservice.data.remote.firebase.AuthFirebaseSource
import com.example.localservice.domain.model.User
import com.example.localservice.domain.model.UserRole
import com.example.localservice.domain.repository.AuthRepository
import com.example.localservice.util.Result
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val source: AuthFirebaseSource,
    private val userDao: UserDao
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser

            if (firebaseUser == null) {
                trySend(null)
            } else {
                launch(Dispatchers.IO) {   // usa la scope del callbackFlow, no una nueva
                    val result = source.getUserFromFirestore(firebaseUser.uid)
                    if (result is Result.Success) {
                        userDao.upsert(result.data.toEntity())
                    }
                    val cached = userDao.getUserById(firebaseUser.uid)
                    trySend(cached?.toDomain())
                }
            }
        }

        FirebaseAuth.getInstance().addAuthStateListener(listener)
        awaitClose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val result = source.login(email, password)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { userDao.upsert(result.data.toEntity()) }
        }
        return result
    }

    override suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        val result = source.signInWithGoogle(account)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { userDao.upsert(result.data.toEntity()) }
        }
        return result
    }

    override suspend fun setRole(role: UserRole): Result<User> {
        val uid = source.currentFirebaseUser()?.uid ?: return Result.Error("No hay sesión activa")
        val result = source.setRole(uid, role)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { userDao.upsert(result.data.toEntity()) }
        }
        return result
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<User> {
        val result = source.register(name, email, password, phone, role)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { userDao.upsert(result.data.toEntity()) }
        }
        return result
    }

    override suspend fun logout() = source.logout()

    override suspend fun isLoggedIn(): Boolean =
        source.currentFirebaseUser() != null

    override suspend fun updateProfilePhoto(uid: String, photoUrl: String): Result<Unit> {
        val result = source.updateUser(uid, mapOf("photoUrl" to photoUrl))
        if (result is Result.Success) {
            withContext(Dispatchers.IO) {
                val cached = userDao.getUserById(uid)
                cached?.let { userDao.upsert(it.copy(photoUrl = photoUrl)) }
            }
        }
        return result
    }

    override suspend fun updateUserProfile(uid: String, name: String, phone: String): Result<Unit> {
        val result = source.updateUser(uid, mapOf("name" to name, "phone" to phone))
        if (result is Result.Success) {
            withContext(Dispatchers.IO) {
                val cached = userDao.getUserById(uid)
                cached?.let { userDao.upsert(it.copy(name = name, phone = phone)) }
            }
        }
        return result
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return source.updatePassword(newPassword)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return source.sendPasswordResetEmail(email)
    }

    private fun User.toEntity() = UserEntity(
        uid = uid,
        name = name,
        email = email,
        phone = phone,
        role = role.name,
        photoUrl = photoUrl,
        createdAt = createdAt
    )

    private fun UserEntity.toDomain() = User(
        uid = uid,
        name = name,
        email = email,
        phone = phone,
        role = UserRole.valueOf(role),
        photoUrl = photoUrl,
        createdAt = createdAt
    )
}
