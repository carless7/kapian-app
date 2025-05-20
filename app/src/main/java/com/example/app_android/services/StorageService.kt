package com.example.app_android.services

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageService {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadImage(uri: Uri): String {
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        val uploadTask = imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    fun getImageUrlReference(imageUrl: String): StorageReference {
        return storage.getReferenceFromUrl(imageUrl)
    }
}