package com.example.localservice.data.remote.firebase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
            android.util.Log.d("ServiLocal", "PASO 1 - Creando usuario en Auth")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.Error("UID nulo")

            android.util.Log.d("ServiLocal", "PASO 2 - Usuario creado, uid: $uid")
            val user = User(
                uid = uid,
                name = name,
                email = email,
                phone = phone,
                role = role,
                createdAt = System.currentTimeMillis()
            )

            android.util.Log.d("ServiLocal", "PASO 3 - Escribiendo en Firestore")
            firestore.collection("users").document(uid)
                .set(user.toMap())
                .await()

            android.util.Log.d("ServiLocal", "PASO 4 - Todo OK")
            Result.Success(user)
        } catch (e: Exception) {
            android.util.Log.e("ServiLocal", "EXCEPCION: ${e.message}")
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

    // Login con Google: intercambia el idToken por credencial de Firebase.
    // Si el usuario nunca entró, crea su documento en Firestore con role UNKNOWN
    // (el rol se elige después en RolePickerScreen).
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            val idToken = account.idToken ?: return Result.Error("No se pudo obtener el token de Google")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val uid = authResult.user?.uid ?: return Result.Error("UID nulo")

            val existing = getUserFromFirestore(uid)
            if (existing is Result.Success) return existing

            val user = User(
                uid = uid,
                name = account.displayName ?: "",
                email = account.email ?: "",
                phone = "",
                role = UserRole.UNKNOWN,
                photoUrl = account.photoUrl?.toString() ?: "",
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("users").document(uid)
                .set(user.toMap())
                .await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al iniciar sesión con Google", e)
        }
    }

    // Asigna el rol elegido por un usuario nuevo (Google o email/password)
    suspend fun setRole(uid: String, role: UserRole): Result<User> {
        return try {
            firestore.collection("users").document(uid)
                .update(mapOf("role" to role.name))
                .await()
            val updated = getUserFromFirestore(uid)
            if (updated is Result.Success) updated else Result.Error("Usuario no encontrado")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar rol", e)
        }
    }

    suspend fun updateUser(uid: String, data: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update(data)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar usuario", e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.Error("No hay sesión activa")
            user.updatePassword(newPassword).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cambiar contraseña", e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al enviar correo de recuperación", e)
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
