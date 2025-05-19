package com.example.app_android.auth

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?,
    val profilePictureUrl: String?,
    val updatedAt: Long
)