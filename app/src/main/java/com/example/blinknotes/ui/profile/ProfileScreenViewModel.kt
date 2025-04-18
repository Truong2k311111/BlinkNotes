package com.example.blinknotes.ui.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.blinknotes.data.helper.FirestoreHelper.getUser
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.RecentPost
import com.example.blinknotes.ui.home.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.net.toUri

//
//data class Post(
//    val postId: String,
//    val userId: String,
//    val imageUrl: String,
//    val likes: Int,
//    val userProfileImage: String,
//    val username: String
//)
data class PostWithUser(
    val post: Post,
    val user: User?
)
class ProfileScreenViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _postsWithUsers = mutableStateListOf<PostWithUser>()
    val postsWithUsers: List<PostWithUser> get() = _postsWithUsers

    private val loadedUsers = mutableMapOf<String, User>()

    private val _drafts = mutableStateListOf<Post>()
    val drafts: List<Post> get() = _drafts



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
    fun getUserLikedPostsWithUser(
        userId: String,
        callback: (List<PostWithUser>) -> Unit
    ) {
        db.collection("likes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { likesSnapshot ->
                val likedPostIds = likesSnapshot.documents.mapNotNull { it.getString("postId") }
                    .filter { it.isNotBlank() } // ✅ lọc bỏ ID rỗng

                if (likedPostIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                db.collection("posts")
                    .whereIn(FieldPath.documentId(), likedPostIds)
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val postsList = mutableListOf<Post>()
                        val userIdsSet = mutableSetOf<String>()

                        // Collect user IDs from posts (either userId or userIdCmt)
                        for (doc in postsSnapshot.documents) {
                            try {
                                val id = doc.id
                                val userId = doc.getString("userId") ?: ""  // User who created the post
                                val userIdCmt = doc.getString("userIdCmt") ?: "" // User who commented on the post
                                val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                                val firstImageUrl = imageUrls.firstOrNull() ?: ""
                                val caption = doc.getString("caption") ?: ""
                                val content = doc.getString("content") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: 0L
                                val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                                val visibility = doc.getString("visibility") ?: "public"
                                val tags = doc.get("tags") as? List<String> ?: emptyList()

                                postsList.add(
                                    Post(
                                        id, userId, userIdCmt, imageUrls, firstImageUrl, caption,
                                        content, createdAt, likesCount, commentsCount, visibility, tags
                                    )
                                )
                                // Collect the unique user IDs (post creator and comment creator)
                                if (userId.isNotBlank()) userIdsSet.add(userId)
                                if (userIdCmt.isNotBlank()) userIdsSet.add(userIdCmt)
                            } catch (e: Exception) {
                                Log.e("PostLoad", "Error parsing post: ${e.message}")
                            }
                        }

                        if (userIdsSet.isEmpty()) {
                            // No users to load
                            val result = postsList.map { PostWithUser(it, null) }
                            callback(result)
                            return@addOnSuccessListener
                        }

                        val userMap = mutableMapOf<String, User?>()
                        val userFetchCount = userIdsSet.size
                        var usersFetched = 0

                        // Fetch users for each userId in userIdsSet
                        userIdsSet.forEach { id ->
                            getUser(id) { user ->
                                userMap[id] = user
                                usersFetched++

                                // Once all users are fetched, return the result
                                if (usersFetched == userFetchCount) {
                                    val result = postsList.map { post ->
                                        val user = userMap[post.userId] ?: userMap[post.userIdCmt]
                                        PostWithUser(post, user)
                                    }
                                    callback(result)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PostLoad", "Error getting posts: ${e.message}")
                        callback(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PostLoad", "Error getting likes: ${e.message}")
                callback(emptyList())
            }
    }




    fun loadLikedPosts(userId: String) {
        getUserLikedPostsWithUser(userId) { list ->
            Log.d("LoadLikedPosts", "Số lượng post load được: ${list.size}")

            list.forEachIndexed { index, postWithUser ->
                val username = postWithUser.user?.username ?: "Không có user"
                val postId = postWithUser.post.id
                Log.d("LoadLikedPosts", "[$index] Post ID: $postId - User: $username")
            }

            _postsWithUsers.clear()
            _postsWithUsers.addAll(list)
        }
    }

    fun loadDrafts(userId: String) {
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "draft")
            .get()
            .addOnSuccessListener { result ->
                val draftList = result.documents.mapNotNull { doc ->
                    try {
                        val draftId = doc.id
                        val imageUris = doc.get("imageUris") as? List<String> ?: emptyList()
                        val firstImageUrl = imageUris.firstOrNull() ?: ""
                        val caption = doc.getString("caption") ?: ""
                        val content = doc.getString("content") ?: ""
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        val visibility = doc.getString("visibility") ?: "public"

                        Post(
                            id = draftId,
                            userId = userId,
                            userIdCmt = "",
                            imageUrls = imageUris,
                            firstImageUrl =  firstImageUrl,
                            caption = caption,
                            content = content,
                            createdAt = createdAt,
                            likesCount = 0,
                            commentsCount = 0,
                            visibility = visibility,
                            tags = emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileScreenViewModel", "Error parsing draft: ${e.message}")
                        null
                    }
                }
                _drafts.clear()
                _drafts.addAll(draftList)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error loading drafts: ${e.message}")
            }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("ProfileScreenViewModel", "Post deleted successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error deleting post: ${e.message}")
            }
    }

}
