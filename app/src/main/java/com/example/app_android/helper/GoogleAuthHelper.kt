package com.example.app_android.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.app_android.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class GoogleAuthHelper(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    fun setupGoogleSignIn() {
        try {
            val webClientId = context.getString(R.string.default_web_client_id)

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            throw RuntimeException("Missing default_web_client_id in strings.xml", e)
        }
    }

    fun signIn(launcher: ActivityResultLauncher<Intent>) {
        launchGoogleAuth(launcher, "login")
    }

    fun register(launcher: ActivityResultLauncher<Intent>) {
        launchGoogleAuth(launcher, "register")
    }

    private fun launchGoogleAuth(
        launcher: ActivityResultLauncher<Intent>,
        flowType: String
    ) {
        try {
            val signInIntent = googleSignInClient.signInIntent.apply {
                putExtra("flow_type", flowType)
                putExtra("prompt", "select_account")
            }
            launcher.launch(signInIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Google Play Services not available", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("GoogleAuth", "$flowType error", e)
        }
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): Task<AuthCredential> {
        val account = task.result
        return task.continueWith {
            GoogleAuthProvider.getCredential(account.idToken, null)
        }
    }

    suspend fun firebaseAuthWithGoogle(credential: AuthCredential): Task<*> {
        return auth.signInWithCredential(credential)
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }
}