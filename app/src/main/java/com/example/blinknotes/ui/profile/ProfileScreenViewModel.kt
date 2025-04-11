package com.example.blinknotes.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.RecentPost
import com.example.blinknotes.ui.home.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//
//data class Post(
//    val postId: String,
//    val userId: String,
//    val imageUrl: String,
//    val likes: Int,
//    val userProfileImage: String,
//    val username: String
//)

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
                                profileImage = data["profileImage"] as? String ?: "",
                                coverImage = data["coverImage"] as? String ?: "",
                                blinkNotesId = data["blinkNotesId"] as? String ?: "",
                                bio = data["bio"] as? String ?: "",
                                followers = data["followers"] as? List<String> ?: emptyList(),
                                following = data["following"] as? List<String> ?: emptyList(),
                                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                                followersCount = (data["followersCount"] as? Long)?.toInt() ?: 0,
                                followingCount = (data["followingCount"] as? Long)?.toInt() ?: 0,
                                recentPost = data["recentPost"] as? List<RecentPost> ?: emptyList()
                            )
                            Log.d("ProfileScreen", "Cover image URL: ${user.coverImage}")
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

    fun updateProfile(userId: String, username: String, blinkNotesId: String, bio: String) {
        db.collection("users")
            .document(userId)
            .update(
                mapOf(
                    "username" to username,
                    "blinkNotesId" to blinkNotesId,
                    "bio" to bio
                )
            )
            .addOnSuccessListener {
                Log.d("ProfileScreen", "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error updating profile: ${e.message}")
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

    fun updateCoverImage(userId: String, coverImageUrl: String, onSuccess: () -> Unit = {}) {
        db.collection("users")
            .document(userId)
            .update("coverImage", coverImageUrl)
            .addOnSuccessListener {
                Log.d("ProfileScreen", "Cover image updated successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error updating cover image: ${e.message}")
            }
    }

    fun getUserPosts(userId: String, lastPost: Post? = null, callback: (List<Post>) -> Unit) {
        FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val postsList = result.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val userIdCmt = doc.getString("userIdCmt") ?: ""
                        val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                        val firstImageUrl = imageUrls.firstOrNull() ?: ""
                        val caption = doc.getString("caption") ?: ""
                        val content = doc.getString("content") ?: ""
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                        val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                        val visibility = doc.getString("visibility") ?: "public"
                        val tags = doc.get("tags") as? List<String> ?: emptyList()

                        Post(id, userId, userIdCmt, imageUrls, firstImageUrl, caption, content, createdAt, likesCount, commentsCount, visibility, tags)
                    } catch (e: Exception) {
                        Log.e("ProfileScreenViewModel", "Error parsing post ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                val shuffledPosts = postsList.shuffled()
                val filteredPosts = if (lastPost != null) {
                    shuffledPosts.filter { it.id != lastPost.id }
                } else {
                    shuffledPosts
                }

                callback(filteredPosts.take(10))
            }
            .addOnFailureListener { e ->
                callback(emptyList())
                Log.e("Firestore", "Lỗi khi tải dữ liệu: ${e.message}")
            }
    }

    fun getUserLikedPosts(userId: String, callback: (List<Post>) -> Unit) {
        // Lấy danh sách các bài đăng đã like từ collection "likes"
        db.collection("likes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { likesSnapshot ->
                val likedPostIds = likesSnapshot.documents.mapNotNull { it.getString("postId") }
                
                if (likedPostIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }
                
                // Lấy thông tin chi tiết của các bài đăng đã like
                db.collection("posts")
                    .whereIn("id", likedPostIds)
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val postsList = postsSnapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.id
                                val userIdCmt = doc.getString("userIdCmt") ?: ""
                                val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                                val firstImageUrl = imageUrls.firstOrNull() ?: ""
                                val caption = doc.getString("caption") ?: ""
                                val content = doc.getString("content") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: 0L
                                val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                                val visibility = doc.getString("visibility") ?: "public"
                                val tags = doc.get("tags") as? List<String> ?: emptyList()

                                Post(id, userId, userIdCmt, imageUrls, firstImageUrl, caption, content, createdAt, likesCount, commentsCount, visibility, tags)
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error parsing post: ${e.message}")
                                null
                            }
                        }
                        callback(postsList)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileScreen", "Error getting liked posts: ${e.message}")
                        callback(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error getting likes: ${e.message}")
                callback(emptyList())
            }
    }
} 
