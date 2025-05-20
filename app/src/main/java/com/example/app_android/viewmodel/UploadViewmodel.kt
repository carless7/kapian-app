package com.example.app_android.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class UploadState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

class UploadViewmodel : ViewModel() {
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress

    private val _uploadState = MutableStateFlow(UploadState.IDLE)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _downloadUrl = MutableStateFlow<String?>(null)
    val downloadUrl: StateFlow<String?> = _downloadUrl

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val storageRef = Firebase.storage.reference
    private val firestore = Firebase.firestore

    private val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = currentUserId ?: run {
            Log.e("UploadViewmodel", "No user logged in when saving URL")
            return
        }

        val userData = hashMapOf(
            "imageUrl" to imageUrl,
            "lastUpdated" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("UploadViewmodel", "Image URL saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("UploadViewmodel", "Error saving URL to Firestore", e)
                _errorMessage.value = "Failed to save URL"
            }
    }

    fun fetchStoredImageUrl() {
        val userId = currentUserId ?: run {
            _errorMessage.value = "Not logged in"
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val url = document.getString("imageUrl")
                    if (!url.isNullOrEmpty()) {
                        _downloadUrl.value = url
                        _uploadState.value = UploadState.SUCCESS
                        Log.d("UploadViewmodel", "🔥 Loaded URL: $url")
                    } else {
                        Log.d("UploadViewmodel", "No image URL found in Firestore")
                    }
                } else {
                    Log.d("UploadViewmodel", "No user document found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UploadViewmodel", "Error fetching URL", e)
                _errorMessage.value = "Failed to load image"
            }
    }

    fun uploadImage(imageUri: Uri) {
        _uploadState.value = UploadState.LOADING
        _uploadProgress.value = 0f
        _errorMessage.value = null

        val userId = currentUserId ?: run {
            _uploadState.value = UploadState.ERROR
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                val imageFileName = "images/$userId/${System.currentTimeMillis()}_${imageUri.lastPathSegment ?: "upload"}"
                val imageRef = storageRef.child(imageFileName)

                val uploadTask = imageRef.putFile(imageUri)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    _uploadProgress.value = progress.toFloat()
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Upload failed")
                    }
                    imageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    _downloadUrl.value = url
                    _uploadState.value = UploadState.SUCCESS
                    saveImageUrlToFirestore(url) // Save after successful upload
                }.addOnFailureListener { e ->
                    _uploadState.value = UploadState.ERROR
                    _errorMessage.value = e.message ?: "Upload failed"
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.ERROR
                _errorMessage.value = e.message ?: "Upload failed"
            }
        }
    }

    fun resetState() {
        _uploadState.value = UploadState.IDLE
        _uploadProgress.value = 0f
        _downloadUrl.value = null
        _errorMessage.value = null
    }
}