package com.example.blinknotes.ui.addPhoto

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddPhotoScreenViewModel: ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference


    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Thêm biến lưu các ảnh đã upload (URL)
    private val _uploadedImageUrls = MutableStateFlow<List<String>>(emptyList())
    val uploadedImageUrls: StateFlow<List<String>> = _uploadedImageUrls

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
                // 1. Thêm các ảnh đã upload (URL) vào danh sách
                imageUrls.addAll(_uploadedImageUrls.value)
                // 2. Chỉ upload các ảnh là Uri local (không phải URL)
                for (imageUri in selectedImages.value) {
                    if (imageUri.scheme == "http" || imageUri.scheme == "https") {
                        // Bỏ qua, đã có trong uploadedImageUrls
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

                addPost(userId, imageUrls, caption, content, finalVisibility, finalStatus, tags)
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, if (finalStatus == "draft") "Lưu nháp thành công!" else "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                    // Xóa uploadedImageUrls sau khi đăng thành công
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

    private fun extractHashtags(text: String): List<String> {
        // Regex tìm các hashtag bắt đầu bằng #, không chứa khoảng trắng, dấu câu
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
            "tags" to tags, // Sửa lại ở đây
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
                    // Lưu các URL này vào biến uploadedImageUrls
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
                    // Không tìm thấy draft, trả về draft rỗng với id rỗng
                    onSuccess(Draft("", "", "", "public", emptyList()))
                }
            } catch (e: Exception) {
                Log.e("AddPhotoScreenViewModel", "Error loading draft: ${e.message}")
                // Trả về draft rỗng nếu lỗi
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
