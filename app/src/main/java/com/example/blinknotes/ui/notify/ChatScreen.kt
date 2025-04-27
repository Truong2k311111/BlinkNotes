package com.example.blinknotes.ui.notify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.RadioButton
import coil.compose.AsyncImage
import com.example.blinknotes.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    username: String,
    avatarRes: String,
    isOnline: Boolean,
    hasMoment: Boolean,
    isMomentSeen: Boolean
) {
    var showReportSheet by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    val reportReasons = listOf(
        "Spam", "Lừa đảo", "Ngôn ngữ không phù hợp", "Quấy rối", "Thông tin sai lệch",
        "Nội dung bạo lực", "Nội dung khiêu dâm", "Vi phạm bản quyền", "Tài khoản giả mạo", "Khác"
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.popBackStack() }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasMoment) {
                            val borderBrush = if (isMomentSeen) {
                                SolidColor(colorResource(id = R.color.gainsboro))
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF00FF99),
                                        Color(0xFF00C78A),
                                        Color(0xFF00BEC2)
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, borderBrush, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = avatarRes,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            AsyncImage(
                                model = avatarRes,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop

                            )
                        }

                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Green, CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Hiện đang hoạt động",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.alpha(if (isOnline) 1f else 0f)
                        )
                    }
                }

                Row {
                    Icon(
                        painter = painterResource(R.drawable.flag),
                        tint = Color.Black,
                        contentDescription = "Report",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showReportSheet = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.dots_horizontal),
                        contentDescription = "More",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            MessageInputBar(
                onSendClick = { message ->
                    println("Message sent: $message")
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Divider(color = Color.LightGray,modifier = Modifier.height(0.5.dp))

            // Main chat content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add chat messages here
                items(20) { index ->
                    val isSender = index % 2 == 0
                    val message = if (isSender) "Hello, how are you?" else "I'm good, thanks!"
                    val timestamp = "12:00 PM"
                    val profileImageUrl = avatarRes // Replace with actual image URL

                    ItemsMesg(
                        isSender = isSender,
                        profileImageUrl = profileImageUrl,
                        message = message,
                        timestamp = timestamp
                    )
                }
            }
        }
    }

    if (showReportSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showReportSheet = false
                selectedReason = ""
                               },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .heightIn(min = 100.dp, max = 500.dp),
                horizontalAlignment = Alignment.CenterHorizontally // căn giữa hết
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Vui lòng chọn lý do",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable {
                                showReportSheet = false
                                selectedReason = ""}
                            .padding(end = 8.dp)
                    )
                }

                Text(
                    text = "Cuộc trò chuyện của bạn sẽ được gửi BlinkNotes xem xét. Chúng tôi sẽ không thông báo cho tài khoản mà bạn báo cáo.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(reportReasons) { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (reason == selectedReason),
                                    onClick = { selectedReason = reason }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reason,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = (reason == selectedReason),
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.Red,
                                    unselectedColor = Color.Red
                                )
                            )
                        }
                    }
                }

                if (selectedReason.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { /* Handle report and block */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Báo cáo và chặn", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { /* Handle report only */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Báo cáo", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    onSendClick: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
            .background(color = colorResource(R.color.gainsboro), RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera Icon
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(Color(0xFF00C78A), CircleShape)
                .clickable { /* Open Camera Action */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.camera), // hoặc dùng icon riêng nếu có
                contentDescription = "Camera",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // TextField
        TextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Nhập tin nhắn...") },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji Icon
                    Icon(
                        painter = painterResource(R.drawable.emoticon_cool_outline),
                        contentDescription = "Emoji",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { /* Open Emoji Picker */ }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send hoặc Open Folder Icon
                    Icon(
                        painter = if (message.isBlank()) painterResource(R.drawable.gallery) else painterResource( R.drawable.send_circle),
                        contentDescription = if (message.isBlank()) "Open Gallery" else "Send",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                if (message.isNotBlank()) {
                                    onSendClick(message)
                                    message = ""
                                } else {
                                    // Open Gallery Action
                                }
                            },
                        tint = if (message.isBlank()) Color.Black else Color.Red
                    )
                }
            },
            singleLine = true
        )
    }
}


@Composable
fun ItemsMesg(
    isSender: Boolean,
    profileImageUrl: String,
    message: String,
    timestamp: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        if (!isSender) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop

            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSender) Color(0xFF00C78A) else Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isSender) 16.dp else 0.dp,
                            bottomEnd = if (isSender) 0.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message,
                    color = if (isSender) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timestamp,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isSender) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop

            )
        }
    }
}
