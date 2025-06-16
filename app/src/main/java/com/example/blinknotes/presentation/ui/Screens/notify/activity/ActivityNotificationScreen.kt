package com.example.blinknotes.presentation.ui.Screens.notify.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.domain.model.ActivityNotification
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.viewModel.ActivityNotificationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityNotificationScreen(
    navController: NavHostController,
    viewModel: ActivityNotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_left),
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
                        model = notification.imageUser ?: R.drawable.accounticon,
                        contentDescription = "User Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(id = R.drawable.accounticon),
                        error = painterResource(id = R.drawable.accounticon),
                        contentScale = ContentScale.Crop
                    )
                }
                "FOLLOWED_USER" -> {
                    AsyncImage(
                        model = notification.imageUser ?: R.drawable.accounticon,
                        contentDescription = "User Profile Image",
                        modifier = Modifier,
                        placeholder = painterResource(id = R.drawable.accounticon),
                        error = painterResource(id = R.drawable.accounticon),
                        contentScale = ContentScale.Crop
                    )
                }
                "FRIEND_POSTED" -> {
                    AsyncImage(
                        model = notification.imageUser ?: R.drawable.accounticon,
                        contentDescription = "User Profile Image",
                        modifier = Modifier,
                        placeholder = painterResource(id = R.drawable.accounticon),
                        error = painterResource(id = R.drawable.accounticon),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Icon(
                        painter = painterResource(id = R.drawable.bell),
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
