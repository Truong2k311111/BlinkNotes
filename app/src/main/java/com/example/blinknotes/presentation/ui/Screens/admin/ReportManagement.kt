package com.example.blinknotes.presentation.ui.Screens.admin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.model.SystemNotification
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
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding( vertical = 8.dp)
            .border( 1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = report.reporterImage,
                        contentDescription = "Reporter Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "@${report.reporterName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(report.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Status indicator
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    color = if (report.isRead)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (report.isRead) "Đã xử lý" else "Chưa xử lý",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (report.isRead)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Report type and content
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Loại báo cáo: ${if (report.type == NotificationType.POST_REPORTED) "Bài viết" else "Người dùng"}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = report.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Reported content info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = report.reportedImage,
                    contentDescription = "Reported",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (report.type == NotificationType.POST_REPORTED) 
                        "Bài viết: ${report.reportedName}" 
                    else 
                        "Người dùng: ${report.reportedName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = "Xem",
                    icon = painterResource(R.drawable.eye_outline),
                    onClick = onViewContent
                )
                if (report.type == NotificationType.POST_REPORTED) {
                    ActionButton(
                        text = "Ẩn",
                        icon = painterResource(R.drawable.eye_off),
                        onClick = onHideContent
                    )
                } else {
                    ActionButton(
                        text = "Cấm",
                        icon = painterResource(R.drawable.lock),
                        onClick = onBanUser
                    )
                }
                ActionButton(
                    text = "Xóa",
                    icon = painterResource( R.drawable.window_close),
                    onClick = onDeleteReport,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
