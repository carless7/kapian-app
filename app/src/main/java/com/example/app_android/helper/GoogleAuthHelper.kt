package com.example.app_android.helper

import android.content.Context
import android.content.Intent
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(R.string.google_app_id.toString()) 
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn(signInLauncher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
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