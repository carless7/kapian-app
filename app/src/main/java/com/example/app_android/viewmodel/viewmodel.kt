package com.example.app_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SharedViewModel : ViewModel() {
    var selectedContact by mutableStateOf<String?>(null)
        private set

    fun setContact(contact: String) {
        selectedContact = contact
    }
}
