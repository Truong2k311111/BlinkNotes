package com.example.blinknotes.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

interface FirebaseStorageApi {
    val storage: FirebaseStorage

    suspend fun uploadFile(path: String, file: File): String
    suspend fun uploadBytes(path: String, bytes: ByteArray): String
    suspend fun uploadUri(path: String, uri: Uri): String
    suspend fun downloadFile(path: String, destinationFile: File)
    suspend fun getDownloadUrl(path: String): Uri
    suspend fun deleteFile(path: String)
    suspend fun listFiles(path: String): List<StorageReference>
}

class FirebaseStorageApiImpl(
    override val storage: FirebaseStorage
) : FirebaseStorageApi {
    override suspend fun uploadFile(path: String, file: File): String {
        val ref = storage.reference.child(path)
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun uploadBytes(path: String, bytes: ByteArray): String {
        val ref = storage.reference.child(path)
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun uploadUri(path: String, uri: Uri): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun downloadFile(path: String, destinationFile: File) {
        val ref = storage.reference.child(path)
        ref.getFile(destinationFile).await()
    }

    override suspend fun getDownloadUrl(path: String): Uri {
        val ref = storage.reference.child(path)
        return ref.downloadUrl.await()
    }

    override suspend fun deleteFile(path: String) {
        val ref = storage.reference.child(path)
        ref.delete().await()
    }

    override suspend fun listFiles(path: String): List<StorageReference> {
        val ref = storage.reference.child(path)
        val result = ref.listAll().await()
        return result.items
    }
} 