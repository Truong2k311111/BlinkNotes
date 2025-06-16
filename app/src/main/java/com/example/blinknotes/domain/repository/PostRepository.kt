package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun createPost(caption: String, content: String, visibility: String, imageUris: List<String>): Post
    suspend fun updatePost(postId: String, caption: String, content: String, visibility: String, imageUris: List<String>): Post
    suspend fun deletePost(postId: String)
    suspend fun getPost(postId: String): Post
    suspend fun getUserPosts(userId: String): List<Post>
    suspend fun getFeedPosts(): List<Post>
    suspend fun likePost(postId: String)
    suspend fun unlikePost(postId: String)
    suspend fun savePost(postId: String)
    suspend fun unsavePost(postId: String)
    suspend fun getLikedPosts(userId: String): List<Post>
    suspend fun getSavedPosts(userId: String): List<Post>
    suspend fun getDrafts(userId: String): List<Post>
    suspend fun saveDraft(caption: String, content: String, visibility: String, imageUris: List<String>): Post
    suspend fun loadDraft(draftId: String): Post?
    suspend fun deleteDraft(draftId: String): Result<Unit>
    suspend fun searchPosts(query: String): List<Post>
    suspend fun getPostsByTag(tag: String): List<Post>
    suspend fun getPostById(postId: String): Post?
    suspend fun checkPostLikeStatus(postId: String, userId: String): Boolean
    suspend fun togglePostLike(postId: String, userId: String)
    suspend fun sharePostInChat(
        postId: String,
        senderId: String,
        receiverId: String,
        imageUrl: String?,
        caption: String,
        content: String
    )
    suspend fun getPostsByUserIds(userIds: List<String>): List<Post>
    suspend fun togglePostSave(postId: String, userId: String)
    suspend fun checkPostSaveStatus(postId: String, userId: String): Boolean
    suspend fun reportPost(postId: String, userId: String, reason: String)
    suspend fun sharePostInChat(postId: String, chatId: String)
    suspend fun getDraftPosts(userId: String): List<Post>
    suspend fun saveDraft(post: Post): Result<String>

    // Add paginated feed fetching method
    suspend fun getFeedPostsPaginated(pageSize: Int, startAfterPostId: String?): Pair<List<Post>, String?>
}