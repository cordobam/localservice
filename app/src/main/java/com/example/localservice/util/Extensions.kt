package com.example.localservice.util

import android.util.Patterns

fun String.isValidEmail(): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPhone(): Boolean =
    this.replace(" ", "").length >= 10

fun String.isValidPassword(): Boolean =
    this.length >= 6

// Convierte un mensaje de error de Firebase a español
fun String.toFriendlyError(): String = when {
    contains("email address is already in use") ->
        "Ese correo ya está registrado. ¿Querés iniciar sesión?"
    contains("no user record") || contains("user-not-found") ->
        "No encontramos una cuenta con ese correo."
    contains("wrong-password") || contains("invalid-credential") ->
        "La contraseña es incorrecta."
    contains("network") ->
        "Sin conexión. Revisá tu internet."
    contains("too-many-requests") ->
        "Demasiados intentos. Esperá un momento."
    else -> "Algo salió mal. Intentá de nuevo."
}
