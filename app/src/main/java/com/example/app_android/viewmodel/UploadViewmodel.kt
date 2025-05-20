package com.example.app_android.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
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
    private val auth: FirebaseAuth = Firebase.auth

    private val database: DatabaseReference = Firebase.database.reference
    private fun getCurrentUserId() = Firebase.auth.currentUser?.uid ?: ""

    private fun saveImageUrlToRealtimeDB(imageUrl: String) {
        Firebase.database.reference
            .child("users")
            .child(getCurrentUserId())
            .child("imageUrl")
            .setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("UploadViewmodel", "URL saved to Realtime DB")
            }
            .addOnFailureListener { e ->
                Log.e("UploadViewmodel", "Error saving URL", e)
            }
    }

    fun fetchStoredImageUrl() {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            _errorMessage.value = "Not logged in"
            return
        }

        database.child("users").child(userId).child("imageUrl").get()
            .addOnSuccessListener { snapshot ->
                val url = snapshot.getValue(String::class.java)
                if (url != null) {
                    _downloadUrl.value = url
                    _uploadState.value = UploadState.SUCCESS
                    Log.d("FetchImage", "Image loaded from DB: $url")
                } else {
                    _errorMessage.value = "No image found"
                    Log.d("FetchImage", "No image URL in database")
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to load image"
                Log.e("FetchImage", "Database read failed", e)
            }
    }

    fun uploadImage(imageUri: Uri) {
        _uploadState.value = UploadState.LOADING
        _uploadProgress.value = 0f
        _downloadUrl.value = null
        _errorMessage.value = null

        val user = auth.currentUser
        if (user == null) {
            _uploadState.value = UploadState.ERROR
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                val imageFileName = "images/${user.uid}/${System.currentTimeMillis()}_${imageUri.lastPathSegment ?: "upload"}"
                val imageRef = storageRef.child(imageFileName)

                val uploadTask = imageRef.putFile(imageUri)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    _uploadProgress.value = progress.toFloat()
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Unknown upload error")
                    }
                    imageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    _downloadUrl.value = uri.toString()
                    _uploadState.value = UploadState.SUCCESS
                    downloadUrl?.let { url ->
                        saveImageUrlToRealtimeDB(url.toString())
                    }
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

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        Firebase.auth.addAuthStateListener { user ->
            fetchStoredImageUrl()
            Log.d("AuthState", "User logged in: ${user.uid}")
        }
    }
}