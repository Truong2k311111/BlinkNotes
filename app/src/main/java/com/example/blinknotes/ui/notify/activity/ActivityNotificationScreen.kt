package com.example.blinknotes.ui.notify.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ActivityNotification(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String? = null,
    val postId: String? = null,
    val commentId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUser : String? = null
)

enum class ActivityType {
    TAGGED_IN_COMMENT,
    FOLLOWED_USER,
    FRIEND_POSTED
}

class ActivityNotificationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _notifications = MutableStateFlow<List<ActivityNotification>>(emptyList())
    val notifications: StateFlow<List<ActivityNotification>> = _notifications

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        if (currentUserId.isEmpty()) return
        db.collection("activity_notifications")
            .whereEqualTo("receiverId", currentUserId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    ActivityNotification(
                        id = doc.getString("id") ?: doc.id,
                        type = data["type"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        userId = data["userId"] as? String,
                        postId = data["postId"] as? String,
                        commentId = data["commentId"] as? String,
                        timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                        imageUser = data["imageUser"] as? String ?: null
                    )
                } ?: emptyList()
                _notifications.value = list
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityNotificationScreen(
    navController: NavHostController,
    viewModel: ActivityNotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = com.example.blinknotes.R.drawable.arrow_left),
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Thông báo hoạt động", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            items(notifications) { notification ->
                ActivityNotificationItem(
                    notification = notification,
                    onClick = {
                        when (notification.type) {
                            "TAGGED_IN_COMMENT" -> {
                                navController.navigate(
                                    Screens.DetaillScreen.route + "/${notification.postId}/${notification.userId}?focusCommentId=${notification.commentId}"
                                )
                            }
                            "FOLLOWED_USER" -> {
                                notification.userId?.let { userId ->
                                    navController.navigate(Screens.ProfileScreen.route + "/$userId")
                                }
                            }
                            "FRIEND_POSTED" -> {
                                notification.postId?.let { postId ->
                                    navController.navigate(Screens.DetaillScreen.route + "/$postId/${notification.userId}")
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun  ActivityNotificationItem(
    notification: ActivityNotification,
    onClick: () -> Unit
) {
    Row(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .align (Alignment.CenterVertically)
                .size(50.dp)
                .clip(CircleShape)
            ,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            when (notification.type) {
                "TAGGED_IN_COMMENT" -> {
                    AsyncImage(
                        model = notification.imageUser ?: com.example.blinknotes.R.drawable.accounticon,
                        contentDescription = "User Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(id = com.example.blinknotes.R.drawable.accounticon),
                        error = painterResource(id = com.example.blinknotes.R.drawable.accounticon),
                        contentScale = ContentScale.Crop
                    )
                }
                "FOLLOWED_USER" -> {
                    AsyncImage(
                        model = notification.imageUser ?: com.example.blinknotes.R.drawable.accounticon,
                        contentDescription = "User Profile Image",
                        modifier = Modifier,
                        placeholder = painterResource(id = com.example.blinknotes.R.drawable.accounticon),
                        error = painterResource(id = com.example.blinknotes.R.drawable.accounticon),
                        contentScale = ContentScale.Crop
                    )
                }
                "FRIEND_POSTED" -> {
                    AsyncImage(
                        model = notification.imageUser ?: com.example.blinknotes.R.drawable.accounticon,
                        contentDescription = "User Profile Image",
                        modifier = Modifier,
                        placeholder = painterResource(id = com.example.blinknotes.R.drawable.accounticon),
                        error = painterResource(id = com.example.blinknotes.R.drawable.accounticon),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Icon(
                        painter = painterResource(id = com.example.blinknotes.R.drawable.bell),
                        contentDescription = "Notification",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notification.content,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = getTimeAgo(notification.timestamp),
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }

        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 0.7.dp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}


fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days < 7 -> "$days ngày trước"
        else -> "${days / 7} tuần trước"
    }
}
