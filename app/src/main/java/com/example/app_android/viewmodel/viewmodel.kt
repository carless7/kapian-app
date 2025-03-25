package com.example.app_android.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.app_android.R

class SharedViewModel : ViewModel() {
    val selectedImageUri = mutableStateOf<Uri?>(null)
    val isUsingResource = mutableStateOf(true)
    val resourceDrawableId = mutableStateOf(R.drawable.dni)

    fun setSelectedImage(uri: Uri?) {
        selectedImageUri.value = uri
        isUsingResource.value = false
    }

    fun resetToDefaultImage() {
        selectedImageUri.value = null
        isUsingResource.value = true
    }
}