package com.example.blinknotes.ui.notify.notificationSysTem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemNotificationScreen(
    navController: NavController
) {
    var notifications by remember { mutableStateOf<List<SystemNotification>>(emptyList()) }
    var selectedNotification by remember { mutableStateOf<SystemNotification?>(null) }
    var showNotificationDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("system_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notifications = documents.mapNotNull { it.toObject(SystemNotification::class.java) }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Thông báo hệ thống",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Không có thông báo nào",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            selectedNotification = notification
                            showNotificationDetails = true
                        }
                    )
                }
            }
        }
    }

    // Notification Details Bottom Sheet
    if (showNotificationDetails && selectedNotification != null) {
        NotificationDetailsBottomSheet(
            notification = selectedNotification!!,
            onDismiss = { showNotificationDetails = false }
        )
    }
}

@Composable
fun NotificationCard(
    notification: SystemNotification,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification Icon
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = getNotificationColor(notification.type).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Notification Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Text(
                    text = dateFormat.format(Date(notification.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!notification.isRead) {
                Surface(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailsBottomSheet(
    notification: SystemNotification,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Chi tiết thông báo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Notification Icon
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
                color = getNotificationColor(notification.type).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notification Details
            DetailItem("Tiêu đề", notification.title)
            DetailItem("Nội dung", notification.content)
            DetailItem("Loại thông báo", getNotificationTypeText(notification.type))
            DetailItem("Thời gian", dateFormat.format(Date(notification.createdAt)))
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

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.USER_BLOCKED -> Icons.Default.Lock
        NotificationType.POST_REPORTED -> Icons.Default.Warning
        NotificationType.SYSTEM_UPDATE -> Icons.Default.Create
        NotificationType.MAINTENANCE -> Icons.Default.Build
        NotificationType.OTHER -> Icons.Default.Info
        NotificationType.USER_REPORTED ->  Icons.Default.Warning
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.USER_BLOCKED -> Color(0xFFE53935) // Red
        NotificationType.POST_REPORTED -> Color(0xFFFFA000) // Amber
        NotificationType.SYSTEM_UPDATE -> Color(0xFF43A047) // Green
        NotificationType.MAINTENANCE -> Color(0xFF1E88E5) // Blue
        NotificationType.OTHER -> Color(0xFF757575) // Grey
        NotificationType.USER_REPORTED -> Color(0xFFFFA000)
    }
}

fun getNotificationTypeText(type: NotificationType): String {
    return when (type) {
        NotificationType.USER_BLOCKED -> "Chặn người dùng"
        NotificationType.POST_REPORTED -> "Báo cáo bài viết"
        NotificationType.SYSTEM_UPDATE -> "Cập nhật hệ thống"
        NotificationType.MAINTENANCE -> "Bảo trì"
        NotificationType.OTHER -> "Khác"
        NotificationType.USER_REPORTED -> "Báo cáo người dùng"
    }
} 