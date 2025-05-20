package com.example.app_android.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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