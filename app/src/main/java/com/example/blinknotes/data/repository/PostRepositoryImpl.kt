package com.example.blinknotes.data.repository

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log

class PostRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : PostRepository {

    override suspend fun getPostById(postId: String): Post? {
        return try {
            val doc = db.collection("posts").document(postId).get().await()
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPostsByUserIds(userIds: List<String>): List<Post> {
        return try {
            Log.d("PostRepositoryImpl", "Getting posts for users: $userIds")
            val posts = mutableListOf<Post>()
            
            for (userId in userIds) {
                try {
                    val result = db.collection("posts")
                        .whereEqualTo("userId", userId)
                        .whereNotEqualTo("status", "draft")
                        .orderBy("status", Query.Direction.ASCENDING)  // For composite index
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .await()
                    
                    Log.d("PostRepositoryImpl", "Found ${result.documents.size} posts for user $userId")
                    
                    result.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Post::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("PostRepositoryImpl", "Error converting document ${doc.id} to Post: ${e.message}")
                            null
                        }
                    }.also { posts.addAll(it) }
                } catch (e: Exception) {
                    Log.e("PostRepositoryImpl", "Error getting posts for user $userId: ${e.message}")
                }
            }
            
            Log.d("PostRepositoryImpl", "Total posts found: ${posts.size}")
            posts
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error in getPostsByUserIds: ${e.message}")
            emptyList()
        }
    }

    override suspend fun togglePostSave(postId: String, userId: String) {
        val postRef = db.collection("posts").document(postId)
        val post = postRef.get().await()
        val savedPosts = post.get("savedPosts") as? List<String> ?: emptyList()

        val newSavedPosts = if (userId in savedPosts) {
            savedPosts - userId
        } else {
            savedPosts + userId
        }

        postRef.update("savedPosts", newSavedPosts).await()
    }

    override suspend fun createPost(
        caption: String,
        content: String,
        visibility: String,
        imageUris: List<String>
    ): Post {
        val postRef = db.collection("posts").document()
        val post = Post(
            id = postRef.id,
            caption = caption,
            content = content,
            imageUrls = imageUris,
            visibility = visibility,
            createdAt = System.currentTimeMillis()
        )
        
        val postData = hashMapOf(
            "userId" to post.userId,
            "caption" to post.caption,
            "content" to post.content,
            "imageUrls" to post.imageUrls,
            "createdAt" to post.createdAt,
            "likesCount" to 0,
            "commentsCount" to 0,
            "visibility" to post.visibility,
            "status" to "active",
            "tags" to emptyList<String>()
        )
        
        postRef.set(postData).await()
        return post
    }

    override suspend fun updatePost(
        postId: String,
        caption: String,
        content: String,
        visibility: String,
        imageUris: List<String>
    ): Post {
        val updates = hashMapOf(
            "caption" to caption,
            "content" to content,
            "imageUrls" to imageUris,
            "visibility" to visibility
        )
        
        db.collection("posts").document(postId).update(updates).await()
        return getPost(postId)
    }

    override suspend fun deletePost(postId: String) {
        db.collection("posts").document(postId).delete().await()
    }

    override suspend fun getPost(postId: String): Post {
        val doc = db.collection("posts").document(postId).get().await()
        return doc.toObject(Post::class.java)?.copy(id = doc.id) 
            ?: throw Exception("Post not found")
    }

    override suspend fun getUserPosts(userId: String): List<Post> {
        val result = db.collection("posts")
            .whereEqualTo("userId", userId)
            .whereNotEqualTo("status", "draft")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        return result.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id) as Post
        }
    }

    override suspend fun getFeedPosts(): List<Post> {
        val result = db.collection("posts")
            .whereNotEqualTo("status", "draft")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        return result.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun likePost(postId: String) {
        val postRef = db.collection("posts").document(postId)
        postRef.update("likesCount", FieldValue.increment(1)).await()
    }

    override suspend fun unlikePost(postId: String) {
        val postRef = db.collection("posts").document(postId)
        postRef.update("likesCount", FieldValue.increment(-1)).await()
    }

    override suspend fun savePost(postId: String) {
        val postRef = db.collection("posts").document(postId)
        postRef.update("isSaved", true).await()
    }

    override suspend fun unsavePost(postId: String) {
        val postRef = db.collection("posts").document(postId)
        postRef.update("isSaved", false).await()
    }

    override suspend fun getLikedPosts(userId: String): List<Post> {
        val result = db.collection("posts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isLiked", true)
            .get()
            .await()
        
        return result.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getSavedPosts(userId: String): List<Post> {
        val result = db.collection("posts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isSaved", true)
            .get()
            .await()
        
        return result.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getDrafts(userId: String): List<Post> {
        val result = db.collection("posts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "draft")
            .get()
            .await()
        
        return result.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun saveDraft(
        caption: String,
        content: String,
        visibility: String,
        imageUris: List<String>
    ): Post {
        val postRef = db.collection("posts").document()
        val post = Post(
            id = postRef.id,
            caption = caption,
            content = content,
            imageUrls = imageUris,
            visibility = visibility,
            status = "draft",
            createdAt = System.currentTimeMillis()
        )
        
        val postData = hashMapOf(
            "userId" to post.userId,
            "caption" to post.caption,
            "content" to post.content,
            "imageUrls" to post.imageUrls,
            "createdAt" to post.createdAt,
            "likesCount" to 0,
            "commentsCount" to 0,
            "visibility" to post.visibility,
            "status" to "draft",
            "tags" to emptyList<String>()
        )
        
        postRef.set(postData).await()
        return post
    }

    override suspend fun loadDraft(draftId: String): Post? {
        return try {
            val doc = db.collection("posts")
                .document(draftId)
                .get()
                .await()
            
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteDraft(draftId: String): Result<Unit> {
        db.collection("posts").document(draftId).delete().await()
        return TODO("Provide the return value")
    }

    override suspend fun togglePostLike(postId: String, userId: String) {
        val postRef = db.collection("posts").document(postId)
        val post = postRef.get().await()
        val likes = post.get("likes") as? List<String> ?: emptyList()
        
        val newLikes = if (userId in likes) {
            likes - userId
        } else {
            likes + userId
        }
        
        postRef.update("likes", newLikes).await()
    }

    override suspend fun checkPostLikeStatus(postId: String, userId: String): Boolean {
        return try {
            val post = db.collection("posts").document(postId).get().await()
            val likes = post.get("likes") as? List<String> ?: emptyList()
            likes.contains(userId)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun checkPostSaveStatus(postId: String, userId: String): Boolean {
        return try {
            val user = db.collection("users").document(userId).get().await()
            val savedPosts = user.get("savedPosts") as? List<String> ?: emptyList()
            savedPosts.contains(postId)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun reportPost(postId: String, userId: String, reason: String) {
        // Implementation of reportPost method
    }

    override suspend fun sharePostInChat(postId: String, chatId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getDraftPosts(userId: String): List<Post> {
        TODO("Not yet implemented")
    }

    override suspend fun saveDraft(post: Post): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun searchPosts(query: String): List<Post> {
        return try {
            val result = db.collection("posts")
                .whereNotEqualTo("status", "draft")
                .get()
                .await()
            
            result.documents.mapNotNull { doc ->
                val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                if (post != null && (post.caption?.contains(query, ignoreCase = true) == true || 
                    post.content?.contains(query, ignoreCase = true) == true)) {
                    post
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error searching posts: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPostsByTag(tag: String): List<Post> {
        val result = db.collection("posts")
            .whereArrayContains("tags", tag)
            .get()
            .await()
        
        return result.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun sharePostInChat(
        postId: String,
        senderId: String,
        receiverId: String,
        imageUrl: String?,
        caption: String,
        content: String
    ) {
        val messageData = hashMapOf(
            "postId" to postId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "imageUrl" to imageUrl,
            "caption" to caption,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )
        
        db.collection("contentchat").add(messageData).await()
    }

    override suspend fun getFeedPostsPaginated(pageSize: Int, startAfterPostId: String?): Pair<List<Post>, String?> {
        return try {
            Log.d("PostRepositoryImpl", "Getting feed posts with pageSize: $pageSize, startAfterPostId: $startAfterPostId")
            
            var query: Query = db.collection("posts")
                .whereNotEqualTo("status", "draft")
                .orderBy("status", Query.Direction.ASCENDING)  // Add this for composite index
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            if (startAfterPostId != null) {
                try {
                    val lastVisibleDoc = db.collection("posts").document(startAfterPostId).get().await()
                    if (lastVisibleDoc.exists()) {
                        query = query.startAfter(lastVisibleDoc)
                        Log.d("PostRepositoryImpl", "Using startAfter with document: ${lastVisibleDoc.id}")
                    } else {
                        Log.w("PostRepositoryImpl", "startAfterPostId document not found: $startAfterPostId. Starting from beginning.")
                    }
                } catch (e: Exception) {
                    Log.e("PostRepositoryImpl", "Error getting last visible document: ${e.message}")
                    // If there's an error getting the last document, start from beginning
                }
            }

            val result = query.get().await()
            Log.d("PostRepositoryImpl", "Query returned ${result.documents.size} documents")
            
            val posts = result.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("PostRepositoryImpl", "Error converting document ${doc.id} to Post: ${e.message}")
                    null
                }
            }

            val lastPostId = if (posts.isNotEmpty()) posts.last().id else null
            Log.d("PostRepositoryImpl", "Returning ${posts.size} posts, lastPostId: $lastPostId")

            Pair(posts, lastPostId)
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error in getFeedPostsPaginated: ${e.message}")
            e.printStackTrace()
            Pair(emptyList(), null)
        }
    }
} 