package com.example.app_android.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class SharedViewModel : ViewModel() {
    private val _isNfcReady = MutableLiveData<Boolean>(false)
    val isNfcReady: LiveData<Boolean> get() = _isNfcReady

    private val _nfcShareableUrl = MutableLiveData<String?>()
    val nfcShareableUrl: MutableLiveData<String?> get() = _nfcShareableUrl

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun uploadImageToFirebase(imageUri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null

            try {
                val url = uploadImage(imageUri)
                _nfcShareableUrl.value = url
                _isNfcReady.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _isNfcReady.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw Exception("User not authenticated")

            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("users/$userId/${UUID.randomUUID()}.png")

            val uploadTask = imageRef.putFile(uri).await()
            uploadTask.storage.downloadUrl.await().toString()
        }
    }

    fun resetNfcState() {
        _isNfcReady.value = false
        _nfcShareableUrl.value = null
    }
}