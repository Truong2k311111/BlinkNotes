package com.example.blinknotes.ui.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.ui.notify.notificationSysTem.NotificationType
import com.example.blinknotes.ui.notify.notificationSysTem.SystemNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportManagement(
    navController: NavController
) {
    var reports by remember { mutableStateOf<List<SystemNotification>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Tất cả") }
    var selectedStatus by remember { mutableStateOf("Tất cả") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<SystemNotification?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("system_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(SystemNotification::class.java) }
                    ?.filter { it.type == NotificationType.USER_REPORTED || it.type == NotificationType.POST_REPORTED }
                    ?: emptyList()
                reports = list
            }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Quản lý báo cáo") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == "Tất cả",
                    onClick = { selectedType = "Tất cả" },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = selectedType == "Bài viết",
                    onClick = { selectedType = "Bài viết" },
                    label = { Text("Bài viết") }
                )
                FilterChip(
                    selected = selectedType == "Người dùng",
                    onClick = { selectedType = "Người dùng" },
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reports.filter { report ->
                val matchesSearch = report.content.contains(searchQuery, ignoreCase = true)
                        || report.reporterName.contains(searchQuery, ignoreCase = true)
                        || report.reportedName.contains(searchQuery, ignoreCase = true)
                val matchesType = when (selectedType) {
                    "Bài viết" -> report.type == NotificationType.POST_REPORTED
                    "Người dùng" -> report.type == NotificationType.USER_REPORTED
                    else -> true
                }
                val matchesStatus = when (selectedStatus) {
                    "Chưa xử lý" -> !report.isRead
                    "Đã xử lý" -> report.isRead
                    else -> true
                }
                matchesSearch && matchesType && matchesStatus
            }) { report ->
                ReportCard(
                    report = report,
                    onViewContent = {
                        if (report.type == NotificationType.POST_REPORTED) {
                            navController.navigate("details/${report.reportedId}/${report.reportedId}")
                        } else {
                            navController.navigate("profile_screen/${report.reportedId}")
                        }
                    },
                    onHideContent = {
                        if (report.type == NotificationType.POST_REPORTED) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("posts").document(report.reportedId)
                                .update("isHidden", true)
                        }
                    },
                    onBanUser = {
                        if (report.type == NotificationType.USER_REPORTED) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(report.reportedId)
                                .update("isBlocked", true)
                        }
                    },
                    onDeleteReport = {
                        selectedReport = report
                        showDeleteConfirmation = true
                    },
                    onMarkHandled = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("system_notifications").document(report.id)
                            .update("isRead", true)
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
                        db.collection("system_notifications").document(selectedReport!!.id)
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
    report: SystemNotification,
    onViewContent: () -> Unit,
    onHideContent: () -> Unit,
    onBanUser: () -> Unit,
    onDeleteReport: () -> Unit,
    onMarkHandled: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
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
                    model = report.reporterImage,
                    contentDescription = "Reporter Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "@${report.reporterName}",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loại: ${if (report.type == NotificationType.POST_REPORTED) "Bài viết" else "Người dùng"}",
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = report.content,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = report.reportedImage,
                    contentDescription = "Reported",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (report.type == NotificationType.POST_REPORTED) "Bài viết: ${report.reportedName}" else "Người dùng: ${report.reportedName}",
                    fontSize = 13.sp
                )
            }
            if (report.reportReason.isNotEmpty()) {
                Text(
                    text = "Lý do: ${report.reportReason}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Ngày: ${dateFormat.format(Date(report.createdAt))}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                if (report.type == NotificationType.POST_REPORTED) {
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
                Button(
                    onClick = onMarkHandled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (report.isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (report.isRead) "Đã xử lý" else "Đánh dấu đã xử lý")
                }
                IconButton(onClick = onDeleteReport) {
                    Icon(Icons.Default.Delete, "Delete Report")
                }
            }
        }
    }
}
