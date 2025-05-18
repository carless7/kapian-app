package com.example.app_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        _currentUser.value = auth.currentUser
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                if (email.isBlank() || password.isBlank()) {
                    _loginState.value = LoginState.Error("Email and password cannot be empty")
                    return@launch
                }

                val result = auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = result.user
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Firebase.auth.currentUser?.reload()?.addOnCompleteListener {
                                _loginState.value = LoginState.Success
                                _currentUser.value = Firebase.auth.currentUser
                            }
                        } else {
                            _loginState.value = LoginState.Error(
                                task.exception?.message ?: "Registration failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateUserFromFirebase() {
        _currentUser.value = auth.currentUser
        if (_currentUser.value != null) {
            _loginState.value = LoginState.Success
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _currentUser.value = null
                _loginState.value = LoginState.Idle
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Logout failed: ${e.message}")
            }
        }
    }
    fun registerWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val result = auth.signInWithCredential(credential).await()

                result.user?.let { user ->
                    _currentUser.value = user
                    _loginState.value = LoginState.Success
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Registration failed")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}