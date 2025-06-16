package com.example.blinknotes.presentation.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.data.model.NotificationBody
import com.example.blinknotes.data.model.SendMessageDto
import com.example.blinknotes.data.remote.FcmApi
import com.example.blinknotes.domain.model.Post
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
import com.example.blinknotes.domain.usecase.post.CreatePostUseCase
import com.example.blinknotes.domain.usecase.post.SaveDraftUseCase
import com.example.blinknotes.domain.usecase.post.LoadDraftUseCase
import com.example.blinknotes.domain.usecase.post.DeleteDraftUseCase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class AddPhotoScreenUiState {
    object Initial : AddPhotoScreenUiState()
    object Loading : AddPhotoScreenUiState()
    object Success : AddPhotoScreenUiState()
    data class Error(val error: AddPhotoScreenError) : AddPhotoScreenUiState()
}

sealed class AddPhotoScreenError {
    data class NetworkError(val message: String) : AddPhotoScreenError()
    data class DatabaseError(val message: String) : AddPhotoScreenError()
    data class AuthError(val message: String) : AddPhotoScreenError()
    data class ValidationError(val message: String) : AddPhotoScreenError()
    data class StorageError(val message: String) : AddPhotoScreenError()
}

@HiltViewModel
class AddPhotoScreenViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val loadDraftUseCase: LoadDraftUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddPhotoScreenUiState>(AddPhotoScreenUiState.Initial)
    val uiState: StateFlow<AddPhotoScreenUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _uploadedImageUrls = MutableStateFlow<List<String>>(emptyList())
    val uploadedImageUrls: StateFlow<List<String>> = _uploadedImageUrls.asStateFlow()

    private val _editingDraft = MutableStateFlow<Post?>(null)
    val editingDraft: StateFlow<Post?> = _editingDraft.asStateFlow()

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("https://blinknotes-api.onrender.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    fun setSelectedImages(images: List<Uri>) {
        _selectedImages.value = images
    }

    fun createPost(caption: String, content: String, visibility: String) {
        if (caption.isBlank() && content.isBlank()) {
            _uiState.value = AddPhotoScreenUiState.Error(AddPhotoScreenError.ValidationError("Caption or content cannot be empty"))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AddPhotoScreenUiState.Loading
                _isLoading.value = true
                
                // Upload images and get URLs
                val imageUrls = _selectedImages.value.map { uri ->
                    if (uri.scheme == "http" || uri.scheme == "https") {
                        uri.toString()
                    } else {
                        uploadImageAndGetUrl(uri)
                    }
                }

                createPostUseCase(caption, content, visibility, imageUrls)
                _uiState.value = AddPhotoScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddPhotoScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AddPhotoScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> AddPhotoScreenError.DatabaseError(e.message ?: "Database error")
                        else -> AddPhotoScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveDraft(caption: String, content: String, visibility: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AddPhotoScreenUiState.Loading
                
                // Upload images and get URLs
                val imageUrls = _selectedImages.value.map { uri ->
                    if (uri.scheme == "http" || uri.scheme == "https") {
                        uri.toString()
                    } else {
                        uploadImageAndGetUrl(uri)
                    }
                }

                saveDraftUseCase(caption, content, visibility, imageUrls)
                _uiState.value = AddPhotoScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddPhotoScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AddPhotoScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> AddPhotoScreenError.DatabaseError(e.message ?: "Database error")
                        else -> AddPhotoScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun loadDraft(draftId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AddPhotoScreenUiState.Loading
                val draft = loadDraftUseCase(draftId)
                _editingDraft.value = draft

                draft?.let {
                    val uris = mutableListOf<Uri>()
                    for (url in it.imageUrls) {
                        try {
                            val uri = Uri.parse(url)
                            if (uri != null) {
                                uris.add(uri)
                            } else {
                                Log.e("AddPhotoScreenVM", "Parsed URI is null for URL: $url")
                            }
                        } catch (e: Exception) {
                            Log.e("AddPhotoScreenVM", "Error parsing URI: $url", e)
                        }
                    }
                    _selectedImages.value = uris
                    _uiState.value = AddPhotoScreenUiState.Success
                } ?: run {
                    _selectedImages.value = emptyList()
                    _editingDraft.value = null
                    _uiState.value = AddPhotoScreenUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = AddPhotoScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AddPhotoScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> AddPhotoScreenError.DatabaseError(e.message ?: "Database error")
                        else -> AddPhotoScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun clearDraft(draftId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AddPhotoScreenUiState.Loading
                deleteDraftUseCase(draftId)
                _uiState.value = AddPhotoScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddPhotoScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AddPhotoScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> AddPhotoScreenError.DatabaseError(e.message ?: "Database error")
                        else -> AddPhotoScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

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
    fun uploadImagesToFirebase(caption: String, content: String, visibility: String, status: String, context: Context, onSuccess: () -> Unit) {
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
    suspend fun uploadImageAndGetUrl(uri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    fun updatePost(
        postId: String,
        caption: String,
        content: String,
        existingImageUrls: List<String>,
        newImageUris: List<Uri>,
        visibility: String,
        status: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val newImageUrls = newImageUris.map { uri ->
                    uploadImageAndGetUrl(uri)
                }

                val finalImageUrls = existingImageUrls + newImageUrls
                val tags = extractHashtags(content)
                val updates = mapOf(
                    "caption" to caption,
                    "content" to content,
                    "imageUrls" to finalImageUrls,
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
        val imageUrls: List<String>
    )
}