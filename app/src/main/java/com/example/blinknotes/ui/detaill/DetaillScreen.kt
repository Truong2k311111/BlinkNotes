package com.example.blinknotes.ui.detaill

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.home.ExploreScreenViewModel
import com.example.blinknotes.ui.home.LoadingAnimation
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.notify.NotifyViewModel
import com.example.blinknotes.ui.profile.ProfileScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetaillScreen(navController: NavHostController,
                  viewModel: ExploreScreenViewModel = viewModel(),
                  postId: String,
                  userId:String,
                  viewModelDetail: DetailScreenViewModel = viewModel(),
                  viewModelNotify: NotifyViewModel = viewModel()

) {
    var showReportSheet by remember { mutableStateOf(false) }
    var showReportConfirmation by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    var post by remember { mutableStateOf<Post?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userIdCmt = currentUser?.uid
    val comments by remember { derivedStateOf { viewModelDetail.comments } }
    val users by viewModel.users.collectAsState()
    val viewModelcmnt: ProfileScreenViewModel = viewModel()
    var usercommnt by remember { mutableStateOf<User?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val friends by viewModelNotify.usersFriend.collectAsState()
    var showShareSheet by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.getPostByPostId(postId) { fetchedPost ->
            post = fetchedPost
            if (fetchedPost != null) {
                viewModelDetail.getUserById(fetchedPost.userId) { fetchedUser ->
                    user = fetchedUser
                }
                viewModelDetail.getComments(postId)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModelcmnt.getCurrentUser(userId = userId) { fetchedUser ->
            usercommnt = fetchedUser
        }
    }
    Scaffold(
        bottomBar = {
            BottomBarDetail(
                postId = post?.id ?: "",
                userId = userIdCmt.toString(),
                parentCommentId = userIdCmt.toString(),
                navController = navController,
                user = usercommnt,
                post = post,
                onclickShare = {showShareSheet = true}
            )
                    },
        topBar = {
            HeaderDetaill(
                navController = navController,
                username = user?.username ?: "",
                profileImage = user?.profileImage ?: "",
                viewModel = viewModelDetail,
                idUserOfPost = post?.userId ?: "",
                click = {
                    if(post?.userId != currentUser?.uid) {
                        navController.navigate(
                            Screens.ProfileScreen.route + "/${post?.userId}"
                        )
                    }
                },
                viewModelNotify = viewModelNotify,
                clickShowReportSheet = {
                    showReportSheet = true
                },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (post == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingAnimation()
                    }
                }
            } else {
                item {
                    val timestamp = viewModel.getTimeAgo(post!!.createdAt)
                    ContentDetail(
                        imageUrls = post!!.imageUrls,
                        pageCount = post!!.imageUrls.size,
                        caption = post!!.caption,
                        content = post!!.content,
                        time = timestamp,
                        onclickImage = { imageUrl ->
                        navController.navigate("detail_image_screen/${Uri.encode(imageUrl)}")
                        },
                        navController = navController
                    )
                }
                item {
                    Divider(modifier = Modifier
                        .fillMaxWidth(0.8f))
                }
                item {
                    val totalComments = comments.sumOf { comment ->
                        1 + comment.replies.size
                    }
                    Text(
                        text = "$totalComments bình luận",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    )
                }
                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Chưa có bình luận nào",
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.bgr)
                                )
                                Text(
                                    text = "Hãy là người đầu tiên bình luận",
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.bgr)
                                )
                            }
                        }
                    }
                } else {
                items(comments) { comment ->
                    LaunchedEffect(comment.userId) {
                        if (!users.containsKey(comment.userId)) {
                            viewModel.fetchUser(comment.userId)
                        }
                    }
                    val user = users[comment.userId]
                    var time = viewModel.getTimeAgo(comment.createdAt)
                    CommentItems(
                        contentComment = comment.content,
                        time = time,
                        avatarUserComment = user?.profileImage ?: "",
                        isAuthor = comment.isAuthor,
                        userName = user?.username ?: "",
                        userCommentId = user?.userId ?: "",
                        userId = userId,
                            postId = postId,
                            parentCommentId = null,
                            replies = comment.replies,
                            commentId = comment.id,
                            likesCount = comment.likes.size,
                            navController = navController
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(colorResource(R.color.bgr))
                                .height(1.dp)
                                .width(32.dp)
                                .align(alignment = Alignment.CenterVertically)
                        )
                        Text(text = "*")
                        Box(
                            modifier = Modifier
                                .background(colorResource(R.color.bgr))
                                .height(1.dp)
                                .width(32.dp)
                                .align(alignment = Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
        if (showReportSheet) {
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
                        text = "Báo cáo bài viết",
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
                            Text("Hủy",
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
                                    showReportConfirmation = true
                                    showReportSheet = false
                                }
                            },
                            modifier = Modifier
                                .widthIn(min = 80.dp)
                                .background(Color(0xFFAC0404), RoundedCornerShape(50.dp))
                            ,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF920015),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50.dp),
                            enabled = reportReason.isNotBlank(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),

                            ) {
                            Text("Gửi báo cáo",
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
        if (showReportConfirmation) {
            AlertDialog(
                onDismissRequest = { showReportConfirmation = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "Xác nhận báo cáo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn gửi báo cáo này không?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModelDetail.reportPost(postId, reportReason)
                            showReportConfirmation = false
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Gửi báo cáo")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showReportConfirmation = false }
                    ) {
                        Text(
                            "Hủy",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
        if (showShareSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showShareSheet = false },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chia sẻ bài viết với bạn bè", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(friends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSharing) {
                                        isSharing = true
                                        val post = post ?: return@clickable
                                            viewModelDetail.sharePostWithUser(
                                                post = post,
                                                senderId = currentUserId,
                                                receiverId = friend.userId,
                                                onSuccess = {
                                                    Toast.makeText(context, "Đã chia sẻ bài viết!", Toast.LENGTH_SHORT).show()
                                                    isSharing = false
                                                    showShareSheet = false
                                                },
                                                onFailure = {
                                                    Toast.makeText(context, "Chia sẻ thất bại!", Toast.LENGTH_SHORT).show()
                                                    isSharing = false
                                                }
                                            )
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = friend.profileImage,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(friend.username, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }

    }
}
@Composable
fun ContentDetail(
    imageUrls: List<String>,
    pageCount: Int,
    caption: String,
    content: String,
    time: String,
    onclickImage : (String) -> Unit,
    navController : NavController
) {
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(color = colorResource(id = R.color.cornsilk))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ){
                            onclickImage(imageUrls[page])                        }
                )
            }
            if (pageCount > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/$pageCount",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(6.dp)
                        .background(
                            if (index == pagerState.currentPage) Color.Black else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = caption,
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HashtagText(
                text = content,
                onHashtagClick = { hashtag ->
                    val encoded = URLEncoder.encode(hashtag, StandardCharsets.UTF_8.toString())
                    navController.navigate("search_screen?query=$encoded")
                }
            )
            Text(
                text = time,
                fontSize = 13.sp,
                color = colorResource(R.color.bgr),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
@Composable
fun HashtagText(
    text: String,
    onHashtagClick: (String) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        val regex = Regex("#\\w+")
        var lastIndex = 0
        for (match in regex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1
            if (lastIndex < start) {
                append(text.substring(lastIndex, start))
            }
            val hashtag = text.substring(start, end)
            pushStringAnnotation(tag = "HASHTAG", annotation = hashtag)
            withStyle(style = SpanStyle(color = Color(0xFF1DA1F2), fontWeight = FontWeight.Bold)) {
                append(hashtag)
            }
            pop()
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle.Default.copy(color = Color.Black),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "HASHTAG", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onHashtagClick(annotation.item)
                }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentItems(
    avatarUserComment: String,
    isAuthor: Boolean,
    userName: String,
    userCommentId: String,
    contentComment: String,
    time: String,
    viewModel: DetailScreenViewModel = viewModel(),
    userId: String,
    blinkNotesId: String = "",
    postId: String,
    parentCommentId: String? = null,
    replies: List<Comment> = emptyList(),
    isReply: Boolean = false,
    replyChain: List<String> = emptyList(),
    commentId: String = "",
    likesCount: Int = 0,
    navController: NavController? = null
) {
    var isFavorite by remember { mutableStateOf(false) }
    var isCommenting by remember { mutableStateOf(false) }
    var showReplies by remember { mutableStateOf(false) }
    var currentParentCommentId by remember { mutableStateOf(parentCommentId) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val comment = remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    var currentUserInfo by remember { mutableStateOf<User?>(null) }
    var parentUserInfo by remember { mutableStateOf<User?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(contentComment) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(parentCommentId) {
        if (parentCommentId != null && parentCommentId != userId) {
            viewModel.getCommentById(parentCommentId) { parentComment ->
                if (parentComment != null) {
                    viewModel.getUserById(parentComment.userId) { user ->
                        parentUserInfo = user
                    }
                }
            }
        }
    }
    LaunchedEffect(commentId, currentUserId) {
        if (commentId.isNotEmpty() && currentUserId.isNotEmpty()) {
            viewModel.checkCommentLikeStatus(commentId, currentUserId) { isLiked ->
                isFavorite = isLiked
            }
        }
    }
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.getUserById(currentUserId) { user ->
                currentUserInfo = user
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 32.dp else 8.dp, bottom = 6.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (currentUserId == userId) {
                        showActionSheet = true
                    }
                }
            )
    ) {
        Row {
            AsyncImage(
                model = avatarUserComment,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = (16.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        color = if (isAuthor) Color(0xFF2196F3) else Color.Black,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable {
                                if(userCommentId != currentUser?.uid) {
                                    navController?.navigate(
                                        Screens.ProfileScreen.route + "/${userCommentId}"
                                    )
                                }
                            }
                    )
                    if (isAuthor) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "• Tác giả",
                            color = Color(0xFF2196F3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (replyChain.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "↪ " + replyChain.joinToString(" • "),
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        placeholder = {
                            Text(
                                text = "Chỉnh sửa nội dung",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = false,
                        maxLines = 5,
                        trailingIcon = {
                            if (editText.isNotBlank()) {
                                IconButton(onClick = { editText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa nội dung"
                                    )
                                }
                            }
                        }
                    )
                    Row {
                        Button(
                            onClick = {
                                viewModel.updateComment(commentId, editText)
                                isEditing = false
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) { Text("Lưu") }
                        Button(
                            onClick = { isEditing = false }
                        ) { Text("Hủy") }
                    }
                } else {
                    TagUserText(
                        text = contentComment,
                        onTagClick = { blinkNotesId ->
                            viewModel.getUserByBlinkNotesId(blinkNotesId) { user ->
                                if (user != null) {
                                    navController?.navigate(
                                        Screens.ProfileScreen.route + "/${user.userId}"
                                    )
                                } else {
                                    Log.e("CommentItems", "User not found for BlinkNotes ID: $blinkNotesId")
                                }
                            }
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 43.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = time,
                    fontSize = 13.sp,
                    color = colorResource(R.color.bgr),
                    modifier = Modifier
                        .padding(end = 12.dp)
                )
                Text(
                    text = "Trả lời",
                    fontSize = 12.sp,
                    color = colorResource(R.color.black),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                isCommenting = true
                                currentParentCommentId = commentId
                            }
                        )
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 14.dp)
            ) {
            IconButton(
                    onClick = {
                        if (commentId.isNotEmpty() && currentUserId.isNotEmpty()) {
                            viewModel.toggleCommentLike(commentId, currentUserId)
                            isFavorite = !isFavorite
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "$likesCount",
                    fontSize = 12.sp,
                    color = colorResource(R.color.bgr),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        if (replies.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(start = 150.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            showReplies = !showReplies
                        }
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (showReplies) "Ẩn trả lời" else "Xem ${replies.size} trả lời",
                        fontSize = 12.sp,
                        color = colorResource(R.color.bgr),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (showReplies) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showReplies) "Ẩn" else "Xem thêm",
                        tint = colorResource(R.color.bgr),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
    }
        if (isCommenting) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = comment.value,
                        onValueChange = { comment.value = it },
                        placeholder = { Text("Viết bình luận...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .border(
                                BorderStroke(2.dp, Color.Gray),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            containerColor = Color.White
                        ),
                    )
                    IconButton(
                        onClick = {
                            if (comment.value.isNotBlank()) {
                                viewModel.addComment(postId, userId, comment.value, currentParentCommentId)
                                comment.value = ""
                                isCommenting = false
                                keyboardController?.hide()
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
                    }
                }
            }
        }
        if (showReplies && replies.isNotEmpty()) {
            Column {
                fun getAllReplies(comments: List<Comment>): List<Comment> {
                    return comments.flatMap { comment ->
                        listOf(comment) + getAllReplies(comment.replies)
                    }
                }
                val allReplies = getAllReplies(replies)
                allReplies.forEach { reply ->
                    var replyUserInfo by remember { mutableStateOf<User?>(null) }
                    LaunchedEffect(reply.userId) {
                        viewModel.getUserById(reply.userId) { user ->
                            replyUserInfo = user
                        }
                    }
                    val newReplyChain = if (isReply) {
                        replyChain + userName
                    } else {
                        listOf(userName)
                    }
                    val replyTime = viewModel.getTimeAgo(reply.createdAt)
                    CommentItems(
                        avatarUserComment = replyUserInfo?.profileImage ?: "",
                        isAuthor = reply.isAuthor,
                        userName = replyUserInfo?.username ?: "",
                        contentComment = reply.content,
                        time = replyTime,
                        userId = userId,
                        postId = postId,
                        parentCommentId = commentId,
                        replies = emptyList(),
                        isReply = true,
                        replyChain = newReplyChain,
                        commentId = reply.id,
                        likesCount = reply.likes.size,
                        navController = navController,
                        userCommentId = reply.userId,
                    )
                }
            }
        }
        if (showActionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showActionSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Tùy chọn bình luận",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    RowOptionItem(
                        text = "Chỉnh sửa",
                        icon = Icons.Default.Edit,
                        onClick = {
                            isEditing = true
                            showActionSheet = false
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    RowOptionItem(
                        text = "Xóa",
                        icon = Icons.Default.Delete,
                        iconTint = Color.Red,
                        textColor = Color.Red,
                        onClick = {
                            showDeleteDialog = true
                            showActionSheet = false
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    RowOptionItem(
                        text = "Hủy",
                        icon = Icons.Default.Close,
                        onClick = {
                            showActionSheet = false
                        }
                    )
                }
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xóa bình luận") },
                text = { Text("Bạn có chắc chắn muốn xóa bình luận này không?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteComment(commentId)
                            showDeleteDialog = false
                        }
                    ) { Text("Xóa") }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}
@Composable
fun TagUserText(
    text: String,
    onTagClick: (String) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        val regex = Regex("@\\w+")
        var lastIndex = 0
        for (match in regex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1
            if (lastIndex < start) {
                append(text.substring(lastIndex, start))
            }
            val tag = text.substring(start, end)
            pushStringAnnotation(tag = "TAG", annotation = tag.removePrefix("@"))
            withStyle(style = SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)) {
                append(tag)
            }
            pop()
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle.Default.copy(color = Color.Black),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TAG", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onTagClick(annotation.item)
                }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderDetaill(
    navController: NavController,
    profileImage: String,
    username: String,
    viewModel: DetailScreenViewModel = viewModel(),
    idUserOfPost: String,
    click: () -> Unit,
    viewModelNotify: NotifyViewModel = viewModel(),
    clickShowReportSheet: () -> Unit = {  }

) {


    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val followStatus by viewModel.followStatus.collectAsState()
    val currentStatus = followStatus[idUserOfPost] ?: DetailScreenViewModel.FollowStatus()
    val isOwnPost = idUserOfPost == userId
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(idUserOfPost) {
        if (userId.isNotEmpty() && idUserOfPost.isNotEmpty()) {
            viewModel.checkFollowStatus(userId, idUserOfPost)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
        }
            AsyncImage(
                model = profileImage,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .clickable {
                        click()
                    },
                contentScale = ContentScale.Crop,

            )
        if (!isOwnPost) {
            Text(
                text = username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.5f)
                    .wrapContentHeight()
                    .clickable {
                        click()
                    }

            )
        }else{
            Text(
                text = username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.5f)
                    .wrapContentHeight()
                    .clickable {
                        click()
                    }
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))
         if (!isOwnPost) {
                Button(
                    modifier = Modifier
                    .height(36.dp)
                        .width(100.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStatus.isFollowing) Color(0xFFE0E0E0) else Color(0xFF2196F3)
                    ),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        if (currentStatus.isFollowing) {
                            showSheet = true
                        } else {
                            viewModel.toggleFollow(userId, idUserOfPost)

                        }
                    }
                ) {
                    val buttonText = when {
                        currentStatus.isFollowing && currentStatus.isFollowedBy -> "Bạn bè"
                        currentStatus.isFollowing -> "Đang Follow"
                        else -> "Follow"
                    }

                    Text(
                        text = buttonText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStatus.isFollowing) Color.Black else Color.White,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (currentStatus.isFollowing) Color.Black else Color.White
                        ),
                        maxLines = 1,
                    )
                }
             Icon(
                 painter = painterResource(R.drawable.flag),
                 tint = Color.Black,
                 contentDescription = "Report",
                 modifier = Modifier
                     .size(28.dp)
                     .clickable { clickShowReportSheet()}
             )
            }

        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .height(1.dp)
        )
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bỏ follow tài khoản này?",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bỏ Follow",
                        color = Color.Red,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ){
                                if (userId.isNotEmpty() && userId.isNotEmpty()) {
                                    viewModel.toggleFollow(userId, idUserOfPost)
                                }
                                showSheet = false
                            }
                        ,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleLarge,

                        )
                Box(modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .background(colorResource(R.color.aliceblue))
                    .height(8.dp))
                    Text(
                        text = "Hủy",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) {
                                showSheet = false
                            }
                    )
            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBarDetail(
    user: User?,
    postId: String,
    userId: String,
    parentCommentId: String,
    navController: NavController,
    post: Post? = null,
    onclickShare: () -> Unit = {  },
) {

    var comment by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isCommenting by remember { mutableStateOf(false) }
    var isShowBottomBar by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: DetailScreenViewModel = viewModel()
    val viewModelEx: ExploreScreenViewModel = viewModel()
    val postLikeStatus by viewModelEx.postLikeStatus.collectAsState()
    val comments by remember { derivedStateOf { viewModel.comments } }
    val totalComments = comments.sumOf { comment ->
        1 + comment.replies.size
    }
    val followingIds = user?.following ?: emptyList()
    val followerIds = user?.followers ?: emptyList()
    val allUsers by viewModelEx.usersAll.collectAsState()
    val friendIds = (followingIds + followerIds).distinct().filter { it != user?.userId }
    val friendUsers = friendIds.mapNotNull { allUsers[it] }
    var showTagSuggestions by remember { mutableStateOf(false) }
    var tagQuery by remember { mutableStateOf("") }

    LaunchedEffect(postId, userId) {
        if (postId.isNotEmpty() && userId.isNotEmpty()) {
            viewModelEx.checkPostLikeStatus(postId, userId)
        }
    }
    LaunchedEffect(postLikeStatus) {
        if (postId.isNotEmpty()) {
            isLiked = postLikeStatus[postId] ?: false
        }
    }
    LaunchedEffect(comment) {
        val regex = Regex("@(\\w*)$")
        val match = regex.find(comment)
        if (match != null) {
            showTagSuggestions = true
            tagQuery = match.groupValues[1]
        } else if (comment.endsWith("@")) {
            showTagSuggestions = true
            tagQuery = ""
        } else {
            showTagSuggestions = false
            tagQuery = ""
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .height(1.dp)
        )
        if (!isShowBottomBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            colorResource(R.color.gainsboro),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            isShowBottomBar = true
                            isCommenting = true
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    Text(text = "Bình luận...", color = Color.Gray)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (postId.isNotEmpty() && userId.isNotEmpty()) {
                                viewModelEx.togglePostLike(postId, userId)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = if (isLiked) R.drawable.heart else R.drawable.heart_outline),
                            contentDescription = null,
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                    }
                    Text(text = "${post?.likesCount ?: 0}", fontSize = 12.sp, color = Color.Gray)
                }
                IconWithText(R.drawable.chat_processing_outline, "${totalComments}", onclickImage = {})
                IconWithText(R.drawable.share_all, "0", onclickImage = {onclickShare()})
                IconWithText(R.drawable.content_save_outline,"", onclickImage = {
                    if (postId.isNotEmpty() && userId.isNotEmpty()) {
                        viewModelEx.togglePostSave(postId, userId)
                    }
                })


            }
        }
        if (isCommenting || comment.isNotBlank() || showEmojiPicker) {
            LaunchedEffect(isCommenting) {
                if (isCommenting) {
                    delay(200)
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
            Column {
                if (showTagSuggestions && friendUsers.isNotEmpty()) {
                    val filteredFriends = if (tagQuery.isBlank()) friendUsers
                        else friendUsers.filter { it.username.contains(tagQuery, ignoreCase = true) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(Color.White)
                    ) {
                        items(filteredFriends) { userTag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newComment = comment.replace(Regex("@\\w*$"), "${userTag.blinkNotesId} ")
                                        comment = newComment
                                        showTagSuggestions = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = userTag.profileImage,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = userTag.username,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user?.profileImage ?: "",
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    TextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Viết bình luận...") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .padding(start = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            containerColor = Color.White
                        ),
                    )
                    IconButton(
                        onClick = {
                            showEmojiPicker = !showEmojiPicker
                            if (showEmojiPicker) {
                                keyboardController?.hide()
                            } else {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (showEmojiPicker) R.drawable.keyboard else R.drawable.emoticon_cool_outline),
                            contentDescription = "Emoji Picker"
                        )
                    }
                    if (comment.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val actualParentId = if (parentCommentId == userId) null else parentCommentId
                                viewModel.addComment(postId, userId, comment, actualParentId)
                                comment = ""
                                isCommenting = false
                                isShowBottomBar = false
                                keyboardController?.hide()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
        if (showEmojiPicker) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .height(1.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 350.dp)
                    .background(Color.White)
            ) {
                EmojiTable(
                    onTextAdded = { emoji ->
                        if (emoji.isEmpty()) {
                            showEmojiPicker = false
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } else if (emoji == "DELETE") {
                            if (comment.isNotEmpty()) {
                                val lastEmojiIndex = comment.lastIndexOf("\\u")
                                if (lastEmojiIndex >= 0) {
                                    val endIndex = lastEmojiIndex + 6
                                    if (endIndex <= comment.length) {
                                        comment = comment.substring(0, lastEmojiIndex)
                                    }
                                } else {
                                    comment = comment.dropLast(1)
                                }
                                focusRequester.requestFocus()
                            }
                        } else {
                            comment = comment + emoji
                            focusRequester.requestFocus()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
@Composable
fun IconWithText(iconRes: Int, count: String, onclickImage: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = { onclickImage() }) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
            )
        }
        Text(text = count, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emoji",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onTextAdded("DELETE") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Delete last emoji",
                        tint = Color.Gray
                    )
                }
                IconButton(
                    onClick = { onTextAdded("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val emojiRows = emojis.chunked(8)
            items(emojiRows) { rowEmojis ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onTextAdded(emoji) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private val emojis = listOf(
    "\ud83d\ude00",
    "\ud83d\ude01",
    "\ud83d\ude02",
    "\ud83d\ude03",
    "\ud83d\ude04",
    "\ud83d\ude05",
    "\ud83d\ude06",
    "\ud83d\ude09",
    "\ud83d\ude0a",
    "\ud83d\ude0b",
    "\ud83d\ude0e",
    "\ud83d\ude0d",
    "\ud83d\ude18",
    "\ud83d\ude17",
    "\ud83d\ude19",
    "\ud83d\ude1a",
    "\u263a",
    "\ud83d\ude42",
    "\ud83e\udd17",
    "\ud83d\ude07",
    "\ud83e\udd13",
    "\ud83e\udd14",
    "\ud83d\ude10",
    "\ud83d\ude11",
    "\ud83d\ude36",
    "\ud83d\ude44",
    "\ud83d\ude0f",
    "\ud83d\ude23",
    "\ud83d\ude25",
    "\ud83d\ude2e",
    "\ud83e\udd10",
    "\ud83d\ude2f",
    "\ud83d\ude2a",
    "\ud83d\ude2b",
    "\ud83d\ude34",
    "\ud83d\ude0c",
    "\ud83d\ude1b",
    "\ud83d\ude1c",
    "\ud83d\ude1d",
    "\ud83d\ude12",
    "\ud83d\ude13",
    "\ud83d\ude14",
    "\ud83d\ude15",
    "\ud83d\ude43",
    "\ud83e\udd11",
    "\ud83d\ude32",
    "\ud83d\ude37",
    "\ud83e\udd12",
    "\ud83e\udd15",
    "\u2639",
    "\ud83d\ude41",
    "\ud83d\ude16",
    "\ud83d\ude1e",
    "\ud83d\ude1f",
    "\ud83d\ude24",
    "\ud83d\ude22",
    "\ud83d\ude2d",
    "\ud83d\ude26",
    "\ud83d\ude27",
    "\ud83d\ude28",
    "\ud83d\ude29",
    "\ud83d\ude2c",
    "\ud83d\ude30",
    "\ud83d\ude31",
    "\ud83d\ude33",
    "\ud83d\ude35",
    "\ud83d\ude21",
    "\ud83d\ude20",
    "\ud83d\ude08",
    "\ud83d\udc7f",
    "\ud83d\udc79",
    "\ud83d\udc7a",
    "\ud83d\udc80",
    "\ud83d\udc7b",
    "\ud83d\udc7d",
    "\ud83e\udd16",
    "\ud83d\udca9",
    "\ud83d\ude3a",
    "\ud83d\ude38",
    "\ud83d\ude39",
    "\ud83d\ude3b",
    "\ud83d\ude3c",
    "\ud83d\ude3d",
    "\ud83d\ude40",
    "\ud83d\ude3f",
    "\ud83d\ude3e",
    "\ud83d\udc66",
    "\ud83d\udc67",
    "\ud83d\udc68",
    "\ud83d\udc69",
    "\ud83d\udc74",
    "\ud83d\udc75",
    "\ud83d\udc76",
    "\ud83d\udc71",
    "\ud83d\udc6e",
    "\ud83d\udc72",
    "\ud83d\udc73",
    "\ud83d\udc77",
    "\u26d1",
    "\ud83d\udc78",
    "\ud83d\udc82",
    "\ud83d\udd75",
    "\ud83c\udf85",
    "\ud83d\udc70",
    "\ud83d\udc7c",
    "\ud83d\udc86",
    "\ud83d\udc87",
    "\ud83d\ude4d",
    "\ud83d\ude4e",
    "\ud83d\ude45",
    "\ud83d\ude46",
    "\ud83d\udc81",
    "\ud83d\ude4b",
    "\ud83d\ude47",
    "\ud83d\ude4c",
    "\ud83d\ude4f",
    "\ud83d\udde3",
    "\ud83d\udc64",
    "\ud83d\udc65",
    "\ud83d\udeb6",
    "\ud83c\udfc3",
    "\ud83d\udc6f",
    "\ud83d\udc83",
    "\ud83d\udd74",
    "\ud83d\udc6b",
    "\ud83d\udc6c",
    "\ud83d\udc6d",
    "\ud83d\udc8f"
)

@Composable
fun RowOptionItem(
    text: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
        )
    }
}
