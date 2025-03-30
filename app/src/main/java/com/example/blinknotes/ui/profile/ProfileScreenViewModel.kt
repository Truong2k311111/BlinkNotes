package com.example.blinknotes.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blinknotes.ui.home.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileScreenViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun getCurrentUser(callback: (User?) -> Unit) {
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        if (data != null) {
                            val user = User(
                                userId = document.id,
                                username = data["username"] as? String ?: "",
                                email = data["email"] as? String ?: "",
                                profileImage = data["profileImage"] as? String ?: ""
                            )
                            callback(user)
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreen", "Error getting user: ${e.message}")
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    fun getFollowCounts(userId: String, callback: (Int, Int) -> Unit) {
        // Lấy số người đang follow
        db.collection("users")
            .document(userId)
            .collection("following")
            .get()
            .addOnSuccessListener { followingSnapshot ->
                val followingCount = followingSnapshot.size()
                
                // Lấy số người follow
                db.collection("users")
                    .document(userId)
                    .collection("followers")
                    .get()
                    .addOnSuccessListener { followersSnapshot ->
                        val followersCount = followersSnapshot.size()
                        callback(followingCount, followersCount)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileScreen", "Error getting followers count: ${e.message}")
                        callback(followingCount, 0)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error getting following count: ${e.message}")
                callback(0, 0)
            }
    }
} 