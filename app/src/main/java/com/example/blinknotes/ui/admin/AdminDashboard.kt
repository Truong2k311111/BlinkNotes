package com.example.blinknotes.ui.admin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blinknotes.auth.AuthManager
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.home.User
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    navController: NavController,
    context: Context
) {
    var totalUsers by remember { mutableStateOf(0) }
    var totalPosts by remember { mutableStateOf(0) }
    var reportedPosts by remember { mutableStateOf(0) }
    var isAdmin by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdmin = AuthManager.checkAdminStatus(context)
        if (!isAdmin) {
            showError = true
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnSuccessListener { documents ->
            totalUsers = documents.size()
        }
        db.collection("posts").get().addOnSuccessListener { documents ->
            totalPosts = documents.size()
            reportedPosts = documents.count { it.getBoolean("isReported") == true }
        }
    }

    if (showError) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.LoginScreen.route) {
                popUpTo(Screens.AdminDashboard.route) { inclusive = true }
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
                            "Trang quản trị",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            AuthManager.clearAdminStatus(context)
                            navController.navigate(Screens.LoginScreen.route) {
                                popUpTo(Screens.AdminDashboard.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, "Đăng xuất")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                "Chào mừng đến với trang quản trị",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Quản lý và giám sát hoạt động của ứng dụng",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Stats Section
                item {
                    Text(
                        "Thống kê",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Tổng người dùng",
                            value = totalUsers.toString(),
                            icon = Icons.Default.Person,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Tổng bài viết",
                            value = totalPosts.toString(),
                            icon = Icons.Default.Edit,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    StatCard(
                        title = "Bài viết vi phạm",
                        value = reportedPosts.toString(),
                        icon = Icons.Default.Warning,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Navigation Section
                item {
                    Text(
                        "Quản lý",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Navigation Cards
                item {
                    NavigationCard(
                        title = "Quản lý tài khoản",
                        description = "Xem và quản lý tài khoản người dùng",
                        icon = Icons.Default.Person,
                        onClick = { navController.navigate(Screens.AdminUserManagement.route) }
                    )
                }

                item {
                    NavigationCard(
                        title = "Quản lý bài viết",
                        description = "Xem và quản lý bài viết",
                        icon = Icons.Default.Star,
                        onClick = { navController.navigate(Screens.AdminPostManagement.route) }
                    )
                }

                item {
                    NavigationCard(
                        title = "Quản lý báo cáo",
                        description = "Xem và xử lý các báo cáo",
                        icon = Icons.Default.Warning,
                        onClick = { navController.navigate(Screens.AdminReportManagement.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 