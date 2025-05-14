package com.example.app_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.app_android.ui.datastore.UserPreferencesRepository
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val _cardName = MutableLiveData<String>("No card selected")
    val cardName: LiveData<String> = _cardName
    val isDarkMode = userPreferencesRepository.isDarkMode.asLiveData()
    val selectedCard = userPreferencesRepository.selectedCard.asLiveData()

    init {
        viewModelScope.launch {
            userPreferencesRepository.selectedCard.collect { card ->
                _cardName.value = card
            }
        }
    }

    fun setCard(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedCard(name)
            _cardName.value = name
        }
    }

    fun clearCard() {
        viewModelScope.launch {
            userPreferencesRepository.clearSelectedCard()
            _cardName.value = "No card selected"
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveDarkModePreference(enabled)
        }
    }
}