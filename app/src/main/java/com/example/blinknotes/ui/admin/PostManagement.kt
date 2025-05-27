package com.example.blinknotes.ui.admin

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.R
import com.example.blinknotes.ui.home.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import com.example.blinknotes.auth.AuthManager
import com.example.blinknotes.navigation.Screens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostManagement(
    navController: NavController,
    context: Context
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showPostDetails by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showHideConfirmation by remember { mutableStateOf(false) }
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
                posts = documents.mapNotNull { it.toObject(Post::class.java) }
                
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
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Tìm kiếm theo caption, ID, username") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Filter Chips
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

                // Post List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts.filter {
                        (it.caption.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true) ||
                        users[it.userId]?.username?.contains(searchQuery, ignoreCase = true) == true) &&
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

    // Post Details Bottom Sheet
    if (showPostDetails && selectedPost != null) {
        PostDetailsBottomSheet(
            post = selectedPost!!,
            user = users[selectedPost!!.userId],
            onDismiss = { showPostDetails = false },
            onHidePost = {
                showHideConfirmation = true
                showPostDetails = false
            },
            onDeletePost = {
                showDeleteConfirmation = true
                showPostDetails = false
            }
        )
    }

    // Delete Confirmation Dialog
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

    // Hide Confirmation Dialog
    if (showHideConfirmation && selectedPost != null) {
        AlertDialog(
            onDismissRequest = { showHideConfirmation = false },
            title = { Text("Xác nhận ẩn") },
            text = { Text("Bạn có chắc muốn ẩn bài viết này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("posts").document(selectedPost!!.id)
                            .update("isHidden", true)
                        showHideConfirmation = false
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
            // Post Image
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
                // User Info
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

                // Post Content
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Post Stats
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

            // Post Image
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(14.dp))

            // User Info
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

            // Post Details
            DetailItem("ID bài viết", post.id)
            DetailItem("Caption", post.caption)
            DetailItem("Lượt thích", post.likesCount.toString())
            DetailItem("Bình luận", post.commentsCount.toString())
            DetailItem("Trạng thái", if (post.isHidden) "Đã ẩn" else "Hiển thị")
            if (post.isReported) {
                DetailItem("Báo cáo", "Đã báo cáo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onHidePost,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ẩn bài viết")
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

