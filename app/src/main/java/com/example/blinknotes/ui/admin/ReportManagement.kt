package com.example.blinknotes.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.ui.home.Report
import com.example.blinknotes.ui.home.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportManagement(
    navController: NavController
) {
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedReportType by remember { mutableStateOf("Tất cả") }
    var selectedStatus by remember { mutableStateOf("Tất cả") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<Report?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        
        // Load reports
        db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                reports = documents.mapNotNull { it.toObject(Report::class.java) }
                
                // Load users
                val userIds = reports.map { it.reporterId }.toSet()
                userIds.forEach { userId ->
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            document.toObject(User::class.java)?.let { user ->
                                users = users + (userId to user)
                            }
                        }
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Quản lý báo cáo") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Implement search */ }) {
                    Icon(Icons.Default.Search, "Search")
                }
            }
        )

        // Search and Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm báo cáo...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedReportType == "Tất cả",
                    onClick = { selectedReportType = "Tất cả" },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = selectedReportType == "Bài viết",
                    onClick = { selectedReportType = "Bài viết" },
                    label = { Text("Bài viết") }
                )
                FilterChip(
                    selected = selectedReportType == "Người dùng",
                    onClick = { selectedReportType = "Người dùng" },
                    label = { Text("Người dùng") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == "Tất cả",
                    onClick = { selectedStatus = "Tất cả" },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = selectedStatus == "Chưa xử lý",
                    onClick = { selectedStatus = "Chưa xử lý" },
                    label = { Text("Chưa xử lý") }
                )
                FilterChip(
                    selected = selectedStatus == "Đã xử lý",
                    onClick = { selectedStatus = "Đã xử lý" },
                    label = { Text("Đã xử lý") }
                )
            }
        }

        // Reports List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reports.filter { report ->
                val matchesSearch = report.content.contains(searchQuery, ignoreCase = true) ||
                        users[report.reporterId]?.username?.contains(searchQuery, ignoreCase = true) == true
                
                val matchesType = when (selectedReportType) {
                    "Bài viết" -> report.reportedType == "post"
                    "Người dùng" -> report.reportedType == "user"
                    else -> true
                }
                
                val matchesStatus = when (selectedStatus) {
                    "Chưa xử lý" -> report.status == "pending"
                    "Đã xử lý" -> report.status != "pending"
                    else -> true
                }
                
                matchesSearch && matchesType && matchesStatus
            }) { report ->
                ReportCard(
                    report = report,
                    reporter = users[report.reporterId],
                    onViewContent = {
                        // Navigate to post/user detail
                        if (report.reportedType == "post") {
                            navController.navigate("details/${report.reportedId}")
                        } else {
                            navController.navigate("profile_screen/${report.reportedId}")
                        }
                    },
                    onHideContent = {
                        if (report.reportedType == "post") {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("posts").document(report.reportedId)
                                .update("isHidden", true)
                        }
                    },
                    onBanUser = {
                        if (report.reportedType == "user") {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(report.reportedId)
                                .update("isBlocked", true)
                        }
                    },
                    onDeleteReport = {
                        selectedReport = report
                        showDeleteConfirmation = true
                    }
                )
            }
        }
    }

    if (showDeleteConfirmation && selectedReport != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa báo cáo này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("reports").document(selectedReport!!.id)
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
}

@Composable
fun ReportCard(
    report: Report,
    reporter: User?,
    onViewContent: () -> Unit,
    onHideContent: () -> Unit,
    onBanUser: () -> Unit,
    onDeleteReport: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Reporter Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = reporter?.profileImage,
                    contentDescription = "Reporter Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "@${reporter?.username ?: "Unknown"}",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Report Type
            Text(
                text = "Loại: ${if (report.reportedType == "post") "Bài viết" else "Người dùng"}",
                color = MaterialTheme.colorScheme.primary
            )

            // Report Content
            Text(
                text = report.content,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Reported Content ID
            Text(
                text = "ID: ${report.reportedId}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Date
            Text(
                text = "Ngày: ${dateFormat.format(Date(report.createdAt))}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onViewContent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Xem")
                }

                if (report.reportedType == "post") {
                    Button(
                        onClick = onHideContent,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Ẩn bài viết")
                    }
                } else {
                    Button(
                        onClick = onBanUser,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Khóa tài khoản")
                    }
                }

                IconButton(onClick = onDeleteReport) {
                    Icon(Icons.Default.Delete, "Delete Report")
                }
            }
        }
    }
} 