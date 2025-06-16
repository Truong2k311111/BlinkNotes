package com.example.blinknotes.presentation.ui.Screens.admin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.auth.AuthManager
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.ui.theme.AdminTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostManagement(
    navController: NavController,
    context: Context = LocalContext.current
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showPostDetails by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showHideConfirmation by remember { mutableStateOf(false) }
    var showUnhideConfirmation by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdmin = AuthManager.checkAdminStatus(context)
        if (!isAdmin) {
            showError = true
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                posts = documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                
                val userIds = posts.map { it.userId }.distinct()
                userIds.forEach { userId ->
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            userDoc.toObject(User::class.java)?.let { user ->
                                users = users + (userId to user)
                            }
                        }
                }
            }
    }

    if (showError) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.LoginScreen.route) {
                popUpTo(Screens.AdminPostManagement.route) { inclusive = true }
            }
        }
        return
    }

    AdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Quản lý bài viết",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Tìm kiếm bài viết...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" },
                        label = { Text("Tất cả") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Reported",
                        onClick = { selectedFilter = "Reported" },
                        label = { Text("Đã báo cáo") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Hidden",
                        onClick = { selectedFilter = "Hidden" },
                        label = { Text("Đã ẩn") }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts.filter {
                        (it.caption.contains(searchQuery, ignoreCase = true) ||
                                it.id.contains(searchQuery, ignoreCase = true) ||
                                users[it.userId]?.username?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true) &&
                                (selectedFilter == "All" ||
                                        (selectedFilter == "Reported" && it.isReported) ||
                                        (selectedFilter == "Hidden" && it.isHidden))
                    }) { post ->
                        PostCard(
                            post = post,
                            user = users[post.userId],
                            onPostClick = {
                                selectedPost = post
                                showPostDetails = true
                            }
                        )
                    }
                }
            }
        }
    }
    if (showPostDetails && selectedPost != null) {
        PostDetailsBottomSheet(
            post = selectedPost!!,
            user = users[selectedPost!!.userId],
            onDismiss = { showPostDetails = false },
            onHidePost = {
                if (selectedPost?.isHidden == true) {
                    showUnhideConfirmation = true
                } else {
                    showHideConfirmation = true
                }
                showPostDetails = false
            },
            onDeletePost = {
                showDeleteConfirmation = true
                showPostDetails = false
            }
        )
    }
    if (showDeleteConfirmation && selectedPost != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa bài viết này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("posts").document(selectedPost!!.id)
                            .delete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    if (showHideConfirmation && selectedPost != null) {
        AlertDialog(
            onDismissRequest = { showHideConfirmation = false },
            title = { Text("Xác nhận ẩn bài viết") },
            text = { Text("Bạn có chắc muốn ẩn bài viết này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val postId = selectedPost?.id
                        if (postId != null && postId.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("posts").document(postId)
                                .update("isHidden", true)
                                .addOnSuccessListener {
                                    // Cập nhật lại danh sách posts sau khi ẩn thành công
                                    val updatedPosts = posts.map { post ->
                                        if (post.id == postId) {
                                            post.copy(isHidden = true)
                                        } else {
                                            post
                                        }
                                    }
                                    posts = updatedPosts
                                    Toast.makeText(context, "Ẩn bài viết thành công", Toast.LENGTH_SHORT).show()
                                    showHideConfirmation = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi khi ẩn bài viết: ${e.message}", Toast.LENGTH_SHORT).show()
                                    showHideConfirmation = false
                                }
                        } else {
                            Toast.makeText(context, "Không thể ẩn bài viết: ID không hợp lệ", Toast.LENGTH_SHORT).show()
                            showHideConfirmation = false
                        }
                    }
                ) {
                    Text("Ẩn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirmation = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    if (showUnhideConfirmation && selectedPost != null) {
        AlertDialog(
            onDismissRequest = { showUnhideConfirmation = false },
            title = { Text("Xác nhận hiện bài viết") },
            text = { Text("Bạn có chắc muốn hiện bài viết này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val postId = selectedPost?.id
                        if (postId != null && postId.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("posts").document(postId)
                                .update("isHidden", false)
                                .addOnSuccessListener {
                                    // Cập nhật lại danh sách posts sau khi hiện thành công
                                    val updatedPosts = posts.map { post ->
                                        if (post.id == postId) {
                                            post.copy(isHidden = false)
                                        } else {
                                            post
                                        }
                                    }
                                    posts = updatedPosts
                                    Toast.makeText(context, "Hiện bài viết thành công", Toast.LENGTH_SHORT).show()
                                    showUnhideConfirmation = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi khi hiện bài viết: ${e.message}", Toast.LENGTH_SHORT).show()
                                    showUnhideConfirmation = false
                                }
                        } else {
                            Toast.makeText(context, "Không thể hiện bài viết: ID không hợp lệ", Toast.LENGTH_SHORT).show()
                            showUnhideConfirmation = false
                        }
                    }
                ) {
                    Text("Hiện")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnhideConfirmation = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    user: User?,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        AsyncImage(
                            model = user?.profileImage,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user?.username ?: "Unknown User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = post.id,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.likesCount.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.chat_processing_outline),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.commentsCount.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (post.isReported) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Đã báo cáo",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    if (post.isHidden) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Đã ẩn",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsBottomSheet(
    post: Post,
    user: User?,
    onDismiss: () -> Unit,
    onHidePost: () -> Unit,
    onDeletePost: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Chi tiết bài viết",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    AsyncImage(
                        model = user?.profileImage,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = user?.username ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(post.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            DetailItem("ID bài viết", post.id)
            DetailItem("Caption", post.caption)
            DetailItem("Lượt thích", post.likesCount.toString())
            DetailItem("Bình luận", post.commentsCount.toString())
            DetailItem("Trạng thái", if (post.isHidden) "Đã ẩn" else "Hiển thị")
            if (post.isReported) {
                DetailItem("Báo cáo", "Đã báo cáo")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onHidePost,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (post.isHidden)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (post.isHidden) painterResource(R.drawable.eye_outline) else painterResource(R.drawable.window_close),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (post.isHidden) "Hiện bài viết" else "Ẩn bài viết")
                }
                OutlinedButton(
                    onClick = onDeletePost,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa bài viết")
                }
            }
        }
    }
}

