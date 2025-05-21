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
                for (imageUri in selectedImages.value) {
                    val fileRef = storageRef.child("uploads/${UUID.randomUUID()}.jpg")
                    fileRef.putFile(imageUri).await()
                    val downloadUri = fileRef.downloadUrl.await().toString()
                    imageUrls.add(downloadUri)
                }
                val visibility = if (visibility.isEmpty()) "public" else visibility
                val status = if (status.isEmpty()) "active" else status

                addPost(userId, imageUrls, caption, content,visibility, status)
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
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
    private suspend fun addDraft(
        userId: String,
        caption: String,
        content: String,
        visibility: String,
        imageUris: List<String>
    ) {
        val newDraftRef = db.collection("drafts").document()
        val draft = hashMapOf(
            "draftId" to newDraftRef.id,
            "userId" to userId,
            "caption" to caption,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "imageUris" to imageUris,
            "visibility" to visibility
        )
        newDraftRef.set(draft).await()
    }

    private suspend fun addPost(
        userId: String,
        imageUrls: List<String>,
        caption: String,
        content: String,
        visibility: String,
        status: String
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
            "tags" to listOf("travel", "food"),
            "status" to status
        )
        newPostRef.set(post).await()
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
                val draftDoc = db.collection("drafts").document(draftId).get().await()
                if (draftDoc.exists()) {
                    val draft = Draft(
                        id = draftDoc.id,
                        caption = draftDoc.getString("caption") ?: "",
                        content = draftDoc.getString("content") ?: "",
                        visibility = draftDoc.getString("visibility") ?: "public",
                        imageUris = draftDoc.get("imageUris") as? List<String> ?: emptyList()
                    )
                    onSuccess(draft)
                }
            } catch (e: Exception) {
                Log.e("AddPhotoScreenViewModel", "Error loading draft: ${e.message}")
            }
        }
    }
}
