package com.example.app_android.auth

import android.content.Context
import androidx.core.app.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.app_android.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.util.UUID

class AuthenticationManager(val context: Context) {
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    fun createAccountWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.let { user ->
                            val userData = UserData(
                                userId = user.uid,
                                username = user.displayName,
                                email = user.email,
                                profilePictureUrl = user.photoUrl?.toString(),
                                updatedAt = System.currentTimeMillis()
                            )

                            firestore.collection("users")
                                .document(user.uid)
                                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener {
                                    trySend(AuthResponse.Success)
                                    close()
                                }
                                .addOnFailureListener { e ->

                                    trySend(AuthResponse.Success)
                                    close()
                                }
                        } ?: run {
                            trySend(AuthResponse.Success)
                            close()
                        }
                    } else {
                        val errorMsg = formatErrorMessage(task.exception?.message ?: "Error desconocido")
                        trySend(AuthResponse.Error(errorMsg))
                        close()
                    }
                }
        } catch (e: Exception) {
            trySend(AuthResponse.Error("Error: ${e.message}"))
            close()
        }

        awaitClose {
        }
    }


    fun logInWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow{
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                } else {
                    val errorMsg = formatErrorMessage(task.exception?.message ?: "")
                    trySend(AuthResponse.Error(errorMsg))
                }
            }
        awaitClose()
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val byte = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(byte)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    fun signInWithGoogle(activity: ComponentActivity): Flow<AuthResponse> = callbackFlow {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        try{
            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(
                context= activity,
                request= request
            )
            val credential = result.credential
            if(credential is CustomCredential){
                if(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                    try{
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val firebaseCredential = GoogleAuthProvider
                            .getCredential(
                                googleIdTokenCredential.idToken,
                                null
                            )
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    trySend(AuthResponse.Success)
                                    it.result.user?.let { user ->
                                        val userData = UserData(
                                            userId = user.uid,
                                            username = user.displayName,
                                            email = user.email,
                                            profilePictureUrl = user.photoUrl?.toString(),
                                            updatedAt = System.currentTimeMillis()
                                        )

                                        firestore.collection("users")
                                            .document(user.uid)
                                            .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                            .addOnSuccessListener {
                                                trySend(AuthResponse.Success)
                                            }
                                            .addOnFailureListener { e ->
                                                trySend(AuthResponse.Success)
                                            }
                                    } ?: trySend(AuthResponse.Success)
                                } else {
                                    val errorMsg = formatErrorMessage(it.exception?.message ?: "")
                                    trySend(AuthResponse.Error(errorMsg))
                                }
                            }

                    } catch (e: GoogleIdTokenParsingException){
                        trySend(AuthResponse.Error(message = e.message ?: ""))
                    }
                }
            }
        } catch (e: Exception){
            trySend(AuthResponse.Error(message = e.message ?: ""))
        }
        awaitClose()
    }

    private fun formatErrorMessage(errorMsg: String): String {
        return when {
            errorMsg.contains("The email address is badly formatted") ->
                "El formato del correo electrónico no es válido"
            errorMsg.contains("The password is invalid") ->
                "La contraseña es incorrecta"
            errorMsg.contains("There is no user record") ->
                "No existe cuenta con este correo electrónico"
            errorMsg.contains("The email address is already in use") ->
                "Este correo electrónico ya está registrado"
            errorMsg.contains("Password should be at least 6 characters") ->
                "La contraseña debe tener al menos 6 caracteres"
            else -> errorMsg
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

sealed interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val message: String): AuthResponse
}