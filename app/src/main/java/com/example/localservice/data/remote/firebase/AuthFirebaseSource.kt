package com.example.localservice.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.localservice.domain.model.User
import com.example.localservice.domain.model.UserRole
import com.example.localservice.util.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Toda llamada a Firebase Auth y Firestore relacionada con usuarios
// va en esta clase. El Repository la usa, nunca el ViewModel.
@Singleton
class AuthFirebaseSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun currentFirebaseUser() = auth.currentUser

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.Error("UID nulo")
            getUserFromFirestore(uid)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al iniciar sesión", e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.Error("UID nulo")

            val user = User(
                uid = uid,
                name = name,
                email = email,
                phone = phone,
                role = role,
                createdAt = System.currentTimeMillis()
            )

            // Guarda el perfil en Firestore
            firestore.collection("users").document(uid)
                .set(user.toMap())
                .await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al registrarse", e)
        }
    }

    suspend fun getUserFromFirestore(uid: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) return Result.Error("Usuario no encontrado")

            val user = User(
                uid = uid,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                role = UserRole.valueOf(doc.getString("role") ?: "UNKNOWN"),
                photoUrl = doc.getString("photoUrl") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener usuario", e)
        }
    }

    fun logout() = auth.signOut()

    // Extensión privada para mapear User a Map para Firestore
    private fun User.toMap() = mapOf(
        "uid"       to uid,
        "name"      to name,
        "email"     to email,
        "phone"     to phone,
        "role"      to role.name,
        "photoUrl"  to photoUrl,
        "createdAt" to createdAt
    )
}
