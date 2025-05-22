package com.example.app_android.notifications

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    fun refreshAndSaveToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newToken = task.result
                Log.d(TAG, "New token: $newToken")

                Firebase.auth.currentUser?.uid?.let { userId ->
                    Firebase.firestore.collection("users")
                        .document(userId)
                        .update("fcmToken", newToken)
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Save failed, scheduling retry", e)
                            scheduleTokenRetry()
                        }
                }
            }
        }
    }

    private fun scheduleTokenRetry(delay: Long = 10000) {
        Handler(Looper.getMainLooper()).postDelayed({
            refreshAndSaveToken()
        }, delay)
    }
}