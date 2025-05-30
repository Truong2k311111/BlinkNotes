package com.example.blinknotes.ui.admin

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.auth.AuthManager
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.home.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagement(
    navController: NavController,
    context: Context
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserDetails by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showBlockConfirmation by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdmin = AuthManager.checkAdminStatus(context)
        if (!isAdmin) {
            showError = true
            return@LaunchedEffect
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                users = documents.mapNotNull { it.toObject(User::class.java) }
            }
    }
    if (showError) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.LoginScreen.route) {
                popUpTo(Screens.AdminUserManagement.route) { inclusive = true }
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
                            "Quản lý tài khoản",
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
                    placeholder = { Text("Tìm kiếm theo email, username, ID") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users.filter {
                        it.email.contains(searchQuery, ignoreCase = true) ||
                        it.username.contains(searchQuery, ignoreCase = true) ||
                        it.userId.contains(searchQuery, ignoreCase = true)
                    }) { user ->
                        UserCard(
                            user = user,
                            onUserClick = {
                                selectedUser = user
                                showUserDetails = true
                            }
                        )
                    }
                }
            }
        }
    }
    if (showUserDetails && selectedUser != null) {
        UserDetailsBottomSheet(
            user = selectedUser!!,
            onDismiss = { showUserDetails = false },
            onBlockUser = {
                showBlockConfirmation = true
                showUserDetails = false
            },
            onDeleteUser = {
                showDeleteConfirmation = true
                showUserDetails = false
            }
        )
    }
    if (showDeleteConfirmation && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa tài khoản này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(selectedUser!!.userId)
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
    if (showBlockConfirmation && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmation = false },
            title = { Text("Xác nhận chặn") },
            text = { Text("Bạn có chắc muốn chặn tài khoản này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(selectedUser!!.userId)
                            .update("isBlocked", true)
                        showBlockConfirmation = false
                    }
                ) {
                    Text("Chặn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmation = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                    model = user.profileImage,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (user.isAdmin) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = if (user.isAdmin) "Admin" else "User",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsBottomSheet(
    user: User,
    onDismiss: () -> Unit,
    onBlockUser: () -> Unit,
    onDeleteUser: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Chi tiết người dùng",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                    model = user.profileImage,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            DetailItem("Username", user.username)
            DetailItem("Email", user.email)
            DetailItem("Ngày tham gia", dateFormat.format(Date(user.createdAt)))
            DetailItem("Số người theo dõi", user.followers.size.toString())
            DetailItem("Đang theo dõi", user.following.size.toString())

            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBlockUser,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chặn")
                }

                OutlinedButton(
                    onClick = onDeleteUser,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xóa")
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
} 