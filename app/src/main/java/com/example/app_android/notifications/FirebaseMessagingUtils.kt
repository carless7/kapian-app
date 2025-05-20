package com.example.app_android.notifications

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

object FirebaseMessagingUtils {

    fun updateFcmToken() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Log.w("FCM", "No user logged in")
            return
        }

        Firebase.messaging.token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "FCM token: $token")

                val userRef = Firebase.firestore.collection("users").document(currentUser.uid)
                userRef.update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "FCM token updated in Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.w("FCM", "Error updating FCM token", e)
                    }
            }
    }
}
