package com.example.blinknotes.presentation.ui.Screens.notify

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.domain.model.Message
import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.model.SystemNotification
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.ui.theme.ShimmerEffect
import com.example.blinknotes.presentation.ui.theme.ShimmerMessageItem
import com.example.blinknotes.presentation.viewModel.AddPhotoScreenViewModel
import com.example.blinknotes.presentation.viewModel.ChatViewModel
import com.example.blinknotes.presentation.viewModel.DetailScreenViewModel
import com.example.blinknotes.presentation.viewModel.NotifyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    username: String,
    avatarRes: String,
    isOnline: Boolean,
    hasMoment: Boolean,
    isMomentSeen: Boolean,
    userOtherId: String,
) {
    val viewModelDetail: DetailScreenViewModel = hiltViewModel()
    val viewModelChat: ChatViewModel = hiltViewModel()
    val viewModelNotify: NotifyViewModel = hiltViewModel()

    val context = LocalContext.current
    var showReportSheet by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    val currentUser = viewModelNotify.currentUser.collectAsState().value
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val messages = viewModelChat.messages.collectAsState().value
    val postPreviews = viewModelChat.postPreviews.collectAsState().value
    val imageProfile = currentUser?.profileImage ?: ""
    val loadingMessages = viewModelChat.loadingMessages.collectAsState().value
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showScrollToBottomButton by remember { mutableStateOf(false) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var loadedMessageIds by remember { mutableStateOf(emptySet<String>()) }
    val selectedImages = remember { mutableStateListOf<Uri>() }
    val capturedImageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModelChat.uploadAndSendMessage(
                senderId = currentUserId.toString(),
                receiverId = userOtherId,
                message = "",
                imageUris = uris,
                onSuccess = {
                    Toast.makeText(context, "Message sent!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e ->
                    Toast.makeText(context, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                },
                contentSendImage = "đã gửi ${uris.size} ảnh"
            )
            selectedImages.addAll(uris)
            viewModelChat.updateSelectedImages(selectedImages)
            viewModelChat.addSelectedImages(selectedImages)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri.value != null) {
            viewModelChat.uploadAndSendMessage(
                senderId = currentUserId.toString(),
                receiverId = userOtherId,
                message = "",
                imageUris = listOf(capturedImageUri.value!!),
                onSuccess = {
                    Toast.makeText(context, "Photo sent!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e ->
                    Toast.makeText(
                        context,
                        "Failed to send photo: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                contentSendImage = "Đã gửi ảnh"
            )
            selectedImages.addAll(capturedImageUri.value?.let { listOf(it) } ?: emptyList())
            viewModelChat.updateSelectedImages(selectedImages)
            viewModelChat.addSelectedImages(selectedImages)
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            lazyListState.scrollToItem(0)
        }
        isInitialLoad = false
        viewModelChat.listenForMessages(currentUserId = currentUserId.toString(), otherUserId = userOtherId)
    }

    LaunchedEffect(messages.size) {
        if (!isInitialLoad && messages.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.isScrollInProgress) {
        val isAtBottom = lazyListState.firstVisibleItemIndex + lazyListState.layoutInfo.visibleItemsInfo.size >= lazyListState.layoutInfo.totalItemsCount
        showScrollToBottomButton = !isAtBottom
    }

    fun createSystemNotification(
        type: NotificationType,
        title: String,
        content: String,
        userId: String,
        reportedBy: String = ""
    ) {
        val notification = SystemNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            type = type,
            createdAt = System.currentTimeMillis(),
            isRead = false
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("system_notifications")
            .document(notification.id)
            .set(notification)
    }
    fun blockUser(userId: String, username: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("isBlocked", true)
            .addOnSuccessListener {
                // Create system notification
                createSystemNotification(
                    type = NotificationType.USER_BLOCKED,
                    title = "Người dùng bị chặn",
                    content = "Người dùng $username đã bị chặn",
                    userId = userId
                )
                Toast.makeText(context, "Đã chặn người dùng", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
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
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Green, CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            MessageInputBar(
                onSendClick = { message ->
                    viewModelChat.sendMessage(
                        senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return@MessageInputBar,
                        receiverId = userOtherId,
                        message = message,
                        contentSendImage = ""
                    )
                    keyboardController?.hide()
                },
                onImageSendClick = {
                    imagePickerLauncher.launch("image/*")
                },
                onCameraClick = {
                    val uri = viewModelChat.createImageUri(context)
                    capturedImageUri.value = uri
                    uri?.let { cameraLauncher.launch(it) }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).imePadding()) {
            Divider(color = Color.LightGray, modifier = Modifier.height(0.5.dp))
            if (loadingMessages) {
                LazyColumn {
                    items(5) {
                        ShimmerMessageItem(isSender = false)
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        val isSender = message.senderId == currentUserId
                        val timestamp = viewModelDetail.getTimeAgo(message.timestamp)
                        val postPreview = postPreviews[message.timestamp]
                        val isNewMessage = !loadedMessageIds.contains(message.timestamp.toString())
                        LaunchedEffect(message.timestamp) {
                            loadedMessageIds = loadedMessageIds + message.timestamp.toString()
                        }
                        if (isNewMessage) {
                            ShimmerMessageItem(isSender = isSender)
                        } else {
                            ItemsMesg(
                                isSender = isSender,
                                profileImageUrl = if (isSender) imageProfile else avatarRes,
                                message = message.content,
                                timestamp = timestamp,
                                profileImageSender = imageProfile,
                                imageUrls = message.imageUrls,
                                postPreview = postPreview,
                                onPostPreviewClick = { postId ->
                                    navController.navigate(Screens.DetaillScreen.route+"/$postId/${userOtherId}")
                                },
                                onImageClick = { url ->
                                    navController.navigate("detail_image_screen/${Uri.encode(url)}")
                                }
                            )
                        }
                    }
                }
            }
            if (showScrollToBottomButton) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(messages.size - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(Color.Gray.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_arrow_dow),
                        contentDescription = "Scroll to Bottom",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showReportSheet) {
        var reportReason by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { showReportSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Báo cáo người dùng",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00C78A),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color(0xFF00C78A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Nhập lý do báo cáo") },
                    label = { Text("Lý do báo cáo") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable { reportReason = "" }
                                .padding(8.dp)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 1,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.flag),
                            contentDescription = "Report",
                            tint = Color(0xFF00C78A)
                        )
                    },
                    isError = reportReason.isBlank(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showReportSheet = false },
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .background(Color.LightGray, RoundedCornerShape(50.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(50.dp))
                            .alpha(0.8f)
                            .clickable { showReportSheet = false }
                            .padding(end = 8.dp),
                    ) {
                        Text(
                            "Hủy",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reportReason.isNotBlank()) {
                                viewModelChat.reportUser(userOtherId, reportReason)
                                showReportSheet = false
                                Toast.makeText(context, "Đã gửi báo cáo thành công", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .background(Color(0xFFAC0404), RoundedCornerShape(50.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF920015),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50.dp),
                        enabled = reportReason.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Gửi báo cáo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    onSendClick: (String) -> Unit,
    onImageSendClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    if (isLoading) {
        ShimmerEffect { brush ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
                    .background(brush, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .background(brush, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(brush, RoundedCornerShape(20.dp))
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
                .background(color = colorResource(R.color.gainsboro), RoundedCornerShape(50.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .background(Color(0xFF00C78A), CircleShape)
                    .clickable { onCameraClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.camera),
                    contentDescription = "Camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = if (message.isBlank()) painterResource(R.drawable.gallery) else painterResource(R.drawable.send_circle),
                            contentDescription = if (message.isBlank()) "Open Gallery" else "Send",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    if (message.isNotBlank()) {
                                        onSendClick(message)
                                        message = ""
                                    } else {
                                        onImageSendClick()
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
}

@Composable
fun ItemsMesg(
    isSender: Boolean,
    profileImageUrl: String,
    profileImageSender: String,
    message: String,
    timestamp: String,
    imageUrls: List<String> = emptyList(),
    onImageClick: (String) -> Unit,
    postPreview: Map<String, Any>? = null,
    onPostPreviewClick: ((String) -> Unit)? = null
) {
    var isLoadingImages by remember { mutableStateOf(imageUrls.isNotEmpty()) }
    var loadedImageCount by remember { mutableStateOf(0) }
    if (isLoadingImages) {
        ShimmerMessageItem(isSender = isSender)
    }
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
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomEnd
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .background(Color.Transparent)
                .padding(4.dp),
            horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
        ) {
            if (postPreview != null && message == "[shared_post]") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val postId = postPreview["postId"] as? String ?: return@clickable
                            onPostPreviewClick?.invoke(postId)
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF00C78A)),
                    color = Color(0xFFE6FFF6)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = postPreview["imageUrls"] as? String,
                            contentDescription = "Post Image",
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = postPreview["caption"] as? String ?: "",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                            Text(
                                text = postPreview["content"] as? String ?: "",
                                maxLines = 1,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Xem bài viết",
                                color = Color(0xFF00C78A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (imageUrls.isNotEmpty()) {
                val columns = if (imageUrls.size > 1) 3 else 1
                val imageSize = if (imageUrls.size > 1) 100.dp else 200.dp
                val rows = (imageUrls.size + columns - 1) / columns
                val gridHeight = if (imageUrls.size > 1) {
                    ((imageSize + 4.dp) * rows).coerceAtMost(300.dp)
                } else {
                    imageSize
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(imageUrls.size) { index ->
                            AsyncImage(
                                model = imageUrls[index],
                                contentDescription = "Message Image",
                                modifier = Modifier
                                    .size(imageSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onImageClick(imageUrls[index])
                                    },
                                contentScale = ContentScale.Crop,
                                onLoading = { isLoadingImages = true },
                                onSuccess = {
                                    loadedImageCount++
                                    if (loadedImageCount == imageUrls.size) {
                                        isLoadingImages = false
                                    }
                                },
                                onError = {
                                    loadedImageCount++
                                    if (loadedImageCount == imageUrls.size) {
                                        isLoadingImages = false
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (message.isNotBlank() && !(postPreview != null && message == "[shared_post]")) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSender) Color(0xFF00C78A) else Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(
                                topStart = if (isSender) 16.dp else 0.dp,
                                topEnd = if (isSender) 0.dp else 16.dp,
                                bottomStart = if (isSender) 16.dp else 24.dp,
                                bottomEnd = if (isSender) 24.dp else 16.dp,
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
            }

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
                model = profileImageSender,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
