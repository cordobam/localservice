package com.example.localservice.domain.repository

import com.example.localservice.domain.model.User
import com.example.localservice.domain.model.UserRole
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    // Devuelve el usuario logueado actualmente, o null si no hay sesión
    fun getCurrentUser(): Flow<User?>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<User>

    suspend fun logout()

    suspend fun isLoggedIn(): Boolean
}
