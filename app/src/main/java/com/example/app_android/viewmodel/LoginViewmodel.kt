package com.example.app_android.viewmodel

import androidx.core.app.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_android.auth.AuthResponse
import com.example.app_android.auth.AuthState
import com.example.app_android.auth.AuthenticationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            authManager.logInWithEmail(email, password).collectLatest { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _loginState.value = AuthState.Success()
                    }
                    is AuthResponse.Error -> {
                        _loginState.value = AuthState.Error("Login Error")
                    }
                }
            }
        }
    }

    fun signInWithGoogle(activity: ComponentActivity) {
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            authManager.signInWithGoogle(activity).collect { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _loginState.value = AuthState.Success()
                    }
                    is AuthResponse.Error -> {
                        _loginState.value = AuthState.Error("Login Error")
                    }
                }
            }
        }
    }
}