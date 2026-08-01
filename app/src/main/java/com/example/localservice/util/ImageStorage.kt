package com.example.localservice.util

import android.content.Context
import android.net.Uri
import java.io.File

// Copia una imagen seleccionada con GetContent() (URI content:// temporal)
// al almacenamiento interno de la app, para que la foto sobreviva al reinicio.
// Devuelve la URI file:// persistente, o null si falla la copia.
fun copyImageToInternalStorage(context: Context, uri: Uri, name: String): String? {
    return try {
        val dir = File(context.filesDir, "profile_photos").apply { mkdirs() }
        val extension = context.contentResolver.getType(uri)
            ?.substringAfter("/", "jpg")
            ?.let { if (it in listOf("jpeg", "jpg", "png", "webp", "gif", "bmp")) it else "jpg" }
            ?: "jpg"
        val file = File(dir, "$name.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        null
    }
}
