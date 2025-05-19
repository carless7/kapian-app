package com.example.app_android.viewmodel

import androidx.core.app.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_android.auth.AuthState
import com.example.app_android.auth.AuthenticationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.app_android.auth.AuthResponse

class RegisterViewModel(
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    private val _emailVerificationState = MutableStateFlow<AuthState>(AuthState.Idle)

    fun register(email: String, password: String) {
        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            authManager.createAccountWithEmail(email, password).collectLatest { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _registerState.value = AuthState.Success()
                    }
                    is AuthResponse.Error -> {
                        _registerState.value = AuthState.Error("Register Error")
                    }
                }
            }
        }
    }

    fun signInWithGoogle(activity: ComponentActivity) {
        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            authManager.signInWithGoogle(activity).collect { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _registerState.value = AuthState.Success()
                    }
                    is AuthResponse.Error -> {
                        _registerState.value = AuthState.Error(response.message)
                    }
                }
            }
        }
    }

    fun clearStates() {
        _registerState.value = AuthState.Idle
        _emailVerificationState.value = AuthState.Idle
    }
}