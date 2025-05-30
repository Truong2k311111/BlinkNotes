package com.example.blinknotes.ui.addPhoto

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.ui.notify.FcmApi
import com.example.blinknotes.ui.notify.NotificationBody
import com.example.blinknotes.ui.notify.SendMessageDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.UUID

class AddPhotoScreenViewModel: ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _uploadedImageUrls = MutableStateFlow<List<String>>(emptyList())
    val uploadedImageUrls: StateFlow<List<String>> = _uploadedImageUrls
    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("https://blinknotes-api.onrender.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    fun setUploadedImageUrls(urls: List<String>) {
        _uploadedImageUrls.value = urls
    }
    fun clearUploadedImageUrls() {
        _uploadedImageUrls.value = emptyList()
    }
    fun updateSelectedImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }
    fun addSelectedImages(uris: List<Uri>) {
        _selectedImages.value = _selectedImages.value+uris
    }
    fun removeSelectedImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }
    fun uploadImagesToFirebase( caption: String, content: String,visibility: String,status: String ,context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val imageUrls = mutableListOf<String>()
                imageUrls.addAll(_uploadedImageUrls.value)
                for (imageUri in selectedImages.value) {
                    if (imageUri.scheme == "http" || imageUri.scheme == "https") {
                        continue
                    }
                    val fileRef = storageRef.child("uploads/${UUID.randomUUID()}.jpg")
                    fileRef.putFile(imageUri).await()
                    val downloadUri = fileRef.downloadUrl.await().toString()
                    imageUrls.add(downloadUri)
                }
                val finalVisibility = if (visibility.isEmpty()) "public" else visibility
                val finalStatus = if (status.isEmpty()) "active" else status
                val tags = extractHashtags(content)
                val postId = addPostAndReturnId(userId, imageUrls, caption, content, finalVisibility, finalStatus, tags)
                if (finalStatus != "draft") {
                    sendActivityNotificationToFriendsAndFollowers(userId, postId, caption)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, if (finalStatus == "draft") "Lưu nháp thành công!" else "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                    clearUploadedImageUrls()
                    onSuccess()
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi upload ảnh hoặc lưu dữ liệu!", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                }
            }
        }
    }
    private suspend fun addPostAndReturnId(
        userId: String,
        imageUrls: List<String>,
        caption: String,
        content: String,
        visibility: String,
        status: String,
        tags: List<String>
    ): String {
        val newPostRef = db.collection("posts").document()
        val post = hashMapOf(
            "postId" to newPostRef.id,
            "userId" to userId,
            "imageUrls" to imageUrls,
            "caption" to caption,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "likesCount" to 0,
            "commentsCount" to 0,
            "visibility" to visibility,
            "tags" to tags,
            "status" to status
        )
        newPostRef.set(post).await()
        return newPostRef.id
    }
    private suspend fun sendActivityNotificationToFriendsAndFollowers(
        userId: String,
        postId: String,
        caption: String
    ) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val username = userDoc.getString("username") ?: "Người dùng"
            val imageUser = userDoc.getString("profileImage") ?: ""
            val followers = userDoc.get("followers") as? List<String> ?: emptyList()
            val following = userDoc.get("following") as? List<String> ?: emptyList()
            val friends = followers.intersect(following)
            val receivers = (friends + followers).toSet().filter { it != userId }

            for (receiverId in receivers) {
                val notificationId = UUID.randomUUID().toString()
                val data = mapOf(
                    "id" to notificationId,
                    "type" to "FRIEND_POSTED",
                    "title" to "Bạn bè vừa đăng bài mới",
                    "content" to "$username vừa đăng một bài viết mới: $caption",
                    "userId" to userId,
                    "postId" to postId,
                    "timestamp" to System.currentTimeMillis(),
                    "receiverId" to receiverId,
                    "isRead" to false,
                    "imageUser" to imageUser,
                )
                db.collection("activity_notifications")
                    .document(notificationId)
                    .set(data)
                    .await()
                try {
                    val receiverDoc = db.collection("users").document(receiverId).get().await()
                    val receiverToken = receiverDoc.getString("fcmToken")

                    if (!receiverToken.isNullOrEmpty()) {
                        val messageDto = SendMessageDto(
                            to = receiverToken,
                            notification = NotificationBody(
                                title = "Bạn bè vừa đăng bài mới",
                                body = "$username vừa đăng một bài viết mới: $caption"
                            )
                        )
                        withContext(Dispatchers.IO) {
                            try {
                                api.sendMessage(messageDto)
                                Log.d("NOTIFICATION", "Đã gửi FCM tới $receiverId")
                            } catch (e: Exception) {
                                Log.e("NOTIFICATION", "Lỗi gửi FCM tới $receiverId", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NOTIFICATION", "Lỗi lấy FCM token của $receiverId", e)
                }
            }

        } catch (e: Exception) {
            Log.e("AddPhotoScreenVM", "Lỗi khi gửi thông báo bài viết: ${e.message}")
        }
    }
    private fun extractHashtags(text: String): List<String> {
        val regex = Regex("""#(\w+)""")
        return regex.findAll(text).map { it.value }.toList()
    }
    private suspend fun addPost(
        userId: String,
        imageUrls: List<String>,
        caption: String,
        content: String,
        visibility: String,
        status: String,
        tags: List<String>
    ) {
        val newPostRef = db.collection("posts").document()
        val post = hashMapOf(
            "postId" to newPostRef.id,
            "userId" to userId,
            "imageUrls" to imageUrls,
            "caption" to caption,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "likesCount" to 0,
            "commentsCount" to 0,
            "visibility" to visibility,
            "tags" to tags,
            "status" to status
        )
        newPostRef.set(post).await()
    }
    fun updatePost(
        postId: String,
        caption: String,
        content: String,
        imageUrls: List<String>,
        visibility: String,
        status: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val tags = extractHashtags(content)
                val updates = mapOf(
                    "caption" to caption,
                    "content" to content,
                    "imageUrls" to imageUrls,
                    "visibility" to visibility,
                    "status" to status,
                    "tags" to tags
                )
                db.collection("posts").document(postId).update(updates).await()
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Cập nhật bài viết thành công!", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                    onSuccess()
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi cập nhật bài viết!", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                }
            }
        }
    }
    data class Draft(
        val id: String,
        val caption: String,
        val content: String,
        val visibility: String,
        val imageUris: List<String>
    )
    fun loadDraft(draftId: String, onSuccess: (Draft) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val draftDoc = db.collection("posts").document(draftId).get().await()
                if (draftDoc.exists()) {
                    val imageUrls = draftDoc.get("imageUrls") as? List<String> ?: emptyList()
                    setUploadedImageUrls(imageUrls)
                    val draft = Draft(
                        id = draftDoc.id,
                        caption = draftDoc.getString("caption") ?: "",
                        content = draftDoc.getString("content") ?: "",
                        visibility = draftDoc.getString("visibility") ?: "public",
                        imageUris = imageUrls
                    )
                    onSuccess(draft)
                } else {
                    onSuccess(Draft("", "", "", "public", emptyList()))
                }
            } catch (e: Exception) {
                Log.e("AddPhotoScreenViewModel", "Error loading draft: ${e.message}")
                onSuccess(Draft("", "", "", "public", emptyList()))
            }
        }
    }
    fun clearDraft(draftId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("posts").document(draftId).delete().await()
                onSuccess()
            } catch (e: Exception) {
                Log.e("AddPhotoScreenViewModel", "Error clearing draft: ${e.message}")
            }
        }
    }
}
