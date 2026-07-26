package com.example.localservice.data.remote.firebase

import android.net.Uri
import com.example.localservice.util.Result
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadProfilePhoto(uid: String, uri: Uri): Result<String> {
        return try {
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("profiles/$uid/$filename")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al subir foto", e)
        }
    }
}
