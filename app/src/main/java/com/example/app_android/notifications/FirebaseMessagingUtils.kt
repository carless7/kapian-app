package com.example.app_android.notifications

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

object FirebaseMessagingUtils {

    fun updateFcmToken() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            Log.w("FCM", "User not logged in — cannot store token.")
            return
        }

        Firebase.messaging.token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Retrieved token: $token")

                val data = hashMapOf("fcmToken" to token)

                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("FCM", "FCM token saved to Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Error saving token", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FCM", "Failed to retrieve token", e)
            }
    }
}