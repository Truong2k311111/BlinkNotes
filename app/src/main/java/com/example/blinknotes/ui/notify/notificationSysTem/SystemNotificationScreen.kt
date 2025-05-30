package com.example.blinknotes.ui.notify.notificationSysTem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.ui.notify.NotifyViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemNotificationScreen(
    navController: NavController,
    viewModel: NotifyViewModel = viewModel()
) {
    var selectedNotification by remember { mutableStateOf<SystemNotification?>(null) }
    var showNotificationDetails by remember { mutableStateOf(false) }
    val notifications by viewModel.systemNotifications.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val filteredNotifications = remember(notifications, currentUserId) {
        notifications.filter { notification ->
            notification.reporterId == currentUserId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông báo hệ thống",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có thông báo nào")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredNotifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            selectedNotification = notification
                            showNotificationDetails = true
                            viewModel.markSystemNotificationAsRead(notification.id)
                        }
                    )
                }
            }
        }
    }

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getNotificationColor(notification.type).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getNotificationIcon(notification.type),
                        contentDescription = null,
                        tint = getNotificationColor(notification.type),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = notification.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            if (notification.type == NotificationType.USER_REPORTED || notification.type == NotificationType.POST_REPORTED) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Người báo cáo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            AsyncImage(
                                model = notification.reporterImage,
                                contentDescription = "Reporter Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notification.reporterName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (notification.type == NotificationType.USER_REPORTED) "Người bị báo cáo" else "Bài viết bị báo cáo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            AsyncImage(
                                model = notification.reportedImage,
                                contentDescription = "Reported Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notification.reportedName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (notification.reportReason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lý do báo cáo: ${notification.reportReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDate(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailsBottomSheet(
    notification: SystemNotification,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getNotificationColor(notification.type).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getNotificationIcon(notification.type),
                        contentDescription = null,
                        tint = getNotificationColor(notification.type),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = notification.content,
                style = MaterialTheme.typography.bodyLarge
            )
            if (notification.type == NotificationType.USER_REPORTED || notification.type == NotificationType.POST_REPORTED) {
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Thông tin người báo cáo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = notification.reporterImage,
                        contentDescription = "Reporter Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = notification.reporterName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "ID: ${notification.reporterId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (notification.type == NotificationType.USER_REPORTED) "Thông tin người bị báo cáo" else "Thông tin bài viết bị báo cáo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = notification.reportedImage,
                        contentDescription = "Reported Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = notification.reportedName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "ID: ${notification.reportedId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (notification.reportReason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Lý do báo cáo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notification.reportReason,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.USER_BLOCKED -> Icons.Default.Lock
        NotificationType.POST_REPORTED -> Icons.Default.Warning
        NotificationType.SYSTEM_UPDATE -> Icons.Default.Create
        NotificationType.MAINTENANCE -> Icons.Default.Build
        NotificationType.OTHER -> Icons.Default.Info
        NotificationType.USER_REPORTED -> Icons.Default.Warning
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.USER_BLOCKED -> Color(0xFFE53935)
        NotificationType.POST_REPORTED -> Color(0xFFFFA000)
        NotificationType.SYSTEM_UPDATE -> Color(0xFF43A047)
        NotificationType.MAINTENANCE -> Color(0xFF1E88E5)
        NotificationType.OTHER -> Color(0xFF757575)
        NotificationType.USER_REPORTED -> Color(0xFFFFA000)
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 