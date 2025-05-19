package com.example.app_android.auth

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: Any? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}