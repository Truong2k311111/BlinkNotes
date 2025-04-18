package com.example.blinknotes.ui.detaill

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import com.example.blinknotes.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.blinknotes.ui.Auth.AuthViewModel
import com.example.blinknotes.ui.home.ExploreScreenViewModel
import com.example.blinknotes.ui.home.LoadingAnimation
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.profile.ProfileScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetaillScreen(navController: NavController,
                  viewModel: ExploreScreenViewModel = viewModel(),
                  postId: String,
                  userId:String,
                  viewModelDetail: DetailScreenViewModel = viewModel(),
                  ) {

    var post by remember { mutableStateOf<Post?>(null) }
    val scrollState = rememberScrollState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userIdCmt = currentUser?.uid
    val comments by remember { derivedStateOf { viewModelDetail.comments } }
    val users by viewModel.users.collectAsState()
    val viewModelcmnt: ProfileScreenViewModel = viewModel()
    var usercommnt by remember { mutableStateOf<User?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(postId) {
        Log.d("DetailScreen", "Fetching post with ID: $postId")
        viewModel.getPostByPostId(postId) { fetchedPost ->
            post = fetchedPost
            if (fetchedPost != null) {
                Log.d("DetailScreen", "Post fetched: $fetchedPost")
                Log.d("DetailScreen", "Fetching user with ID: ${fetchedPost.userId}")
                viewModelDetail.getUserById(fetchedPost.userId) { fetchedUser ->
                    user = fetchedUser
                    if (fetchedUser != null) {
                        Log.d("DetailScreen", "User fetched: $fetchedUser")
                    } else {
                        Log.e("DetailScreen", "User not found for ID: ${fetchedPost.userId}")
                    }
                }
                // Khi có bài post, lấy danh sách comment
                Log.d("DetailScreen", "Fetching comments for post ID: $postId")
                viewModelDetail.getComments(postId)
            } else {
                Log.e("DetailScreen", "Post not found for ID: $postId")
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModelcmnt.getCurrentUser { fetchedUser ->
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
                post = post
            )
                    },
        topBar = {
            HeaderDetaill(
                navController = navController,
                username = user?.username ?: "",
                profileImage = user?.profileImage ?: "",
                viewModel = viewModelDetail,
                idUserOfPost = post?.userId ?: ""
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
                        time = timestamp
                    )
                }
                item {
                    Divider(modifier = Modifier
                        .fillMaxWidth(0.8f))
                }

                item {
                    // Tính tổng số bình luận bao gồm cả reply
                    val totalComments = comments.sumOf { comment ->
                        1 + comment.replies.size // 1 cho comment gốc + số lượng replies
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
                        userId = userId,
                            postId = postId,
                            parentCommentId = null,
                            replies = comment.replies,
                            commentId = comment.id,
                            likesCount = comment.likes.size
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

    }
}
@Composable
fun ContentDetail(
    imageUrls: List<String>,
    pageCount: Int,
    caption: String,
    content: String,
    time: String,
) {
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Tỷ lệ 1:1 cho ảnh vuông
                .background(color = colorResource(id = R.color.cornsilk))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop, // Sử dụng Crop thay vì Fit
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Hiển thị số trang nếu có nhiều hơn 1 ảnh
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

        // Dots indicator
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

        // Content section
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

            Text(
                text = content,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(vertical = 8.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentItems(
    avatarUserComment: String,
    isAuthor: Boolean,
    userName: String,
    contentComment: String,
    time: String,
    viewModel: DetailScreenViewModel = viewModel(),
    userId: String,
    postId: String,
    parentCommentId: String? = null,
    replies: List<Comment> = emptyList(),
    isReply: Boolean = false,
    replyChain: List<String> = emptyList(),
    commentId: String = "",
    likesCount: Int = 0
) {
    var isFavorite by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    var isCommenting by remember { mutableStateOf(false) }
    var showReplies by remember { mutableStateOf(false) }
    var currentParentCommentId by remember { mutableStateOf(parentCommentId) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val comment = remember { mutableStateOf("") }

    // Lấy thông tin user hiện tại
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    var currentUserInfo by remember { mutableStateOf<User?>(null) }

    // Lấy thông tin user của comment cha
    var parentUserInfo by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(parentCommentId) {
        Log.d("CommentItems", "parentCommentId: $parentCommentId, userId: $userId")
        if (parentCommentId != null && parentCommentId != userId) {
            // Lấy thông tin comment cha từ Firestore
            viewModel.getCommentById(parentCommentId) { parentComment ->
                if (parentComment != null) {
                    Log.d("CommentItems", "Parent comment found: $parentComment")
                    // Lấy thông tin user của comment cha
                    viewModel.getUserById(parentComment.userId) { user ->
                        parentUserInfo = user
                        Log.d("CommentItems", "Parent user info received: $user")
                    }
                } else {
                    Log.d("CommentItems", "Parent comment not found")
                }
            }
        } else {
            Log.d("CommentItems", "Skipping parent user fetch: parentCommentId is null or equals userId")
        }
    }

    // Kiểm tra trạng thái like của user hiện tại
    LaunchedEffect(commentId, currentUserId) {
        if (commentId.isNotEmpty() && currentUserId.isNotEmpty()) {
            viewModel.checkCommentLikeStatus(commentId, currentUserId) { isLiked ->
                isFavorite = isLiked
            }
        }
    }

    // Lấy thông tin user hiện tại
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
                if (isAuthor) {
                    if (isReply && parentUserInfo != null) {
                        // Nếu là reply và là tác giả, hiển thị "username * Tác giả > username cha"
                    Row {
                        Text(
                            text = "$userName * ",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                            color = colorResource(R.color.bgr)
                        )
                        Text(
                            text = "Tác giả",
                            fontSize = 13.sp,
                            color = colorResource(R.color.tab_line)
                        )
                            Text(
                                text = " > ${parentUserInfo?.username}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                color = colorResource(R.color.bgr)
                            )
                        }
                    } else {
                        // Nếu là tác giả nhưng không phải reply
                        Row {
                            Text(
                                text = "$userName * ",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                color = colorResource(R.color.bgr)
                            )
                            Text(
                                text = "Tác giả",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                color = colorResource(R.color.tab_line)
                            )
                        }
                    }
                } else {
                    if (isReply && parentUserInfo != null) {
                        // Nếu là reply, hiển thị "username > username cha"
                        Log.d("CommentItems", "Displaying reply: $userName > ${parentUserInfo?.username}")
                        Text(
                            text = "$userName > ${parentUserInfo?.username}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                            color = colorResource(R.color.bgr)
                        )
                    } else {
                        Log.d("CommentItems", "Not displaying reply info: isReply=$isReply, parentUserInfo=${parentUserInfo != null}")
                    Text(
                        text = userName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        color = colorResource(R.color.bgr)
                    )
                    }
                }
                Text(
                    text = contentComment,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 4.dp),
                    color = colorResource(R.color.black)
                )
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
                // Hàm đệ quy để lấy tất cả replies
                fun getAllReplies(comments: List<Comment>): List<Comment> {
                    return comments.flatMap { comment ->
                        listOf(comment) + getAllReplies(comment.replies)
                    }
                }

                // Lấy tất cả replies và hiển thị chúng
                val allReplies = getAllReplies(replies)
                allReplies.forEach { reply ->
                    // Lấy thông tin user của reply
                    var replyUserInfo by remember { mutableStateOf<User?>(null) }

                    LaunchedEffect(reply.userId) {
                        viewModel.getUserById(reply.userId) { user ->
                            replyUserInfo = user
                        }
                    }

                    // Tạo chuỗi username cho reply chain
                    val newReplyChain = if (isReply) {
                        replyChain + userName
                    } else {
                        listOf(userName)
                    }

                    // Lấy thời gian cho reply
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
                        likesCount = reply.likes.size
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderDetaill(
    navController: NavController,
    profileImage: String,
    username: String,
    viewModel: DetailScreenViewModel = viewModel(),
    idUserOfPost: String
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val followStatus by viewModel.followStatus.collectAsState()
    val currentStatus = followStatus[idUserOfPost] ?: DetailScreenViewModel.FollowStatus()
    val isOwnPost = idUserOfPost == userId
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Kiểm tra trạng thái follow khi component được tạo
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
                    .clip(CircleShape),
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
                    .fillMaxWidth(0.4f)
                    .wrapContentHeight()
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
                    .fillMaxWidth(0.7f)
                    .wrapContentHeight()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

            // Chỉ hiển thị nút Follow nếu không phải bài đăng của chính mình
            if (!isOwnPost) {
                Button(
                    modifier = Modifier
                        .height(36.dp)
                        .width(100.dp),
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
            }

        IconButton(
            onClick = { /* Thêm logic Search */ },
            modifier = Modifier
                .size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black
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
    post: Post? = null
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
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Lấy trạng thái like từ ExploreScreenViewModel
    val postLikeStatus by viewModelEx.postLikeStatus.collectAsState()
    val posts by viewModelEx.posts.collectAsState()
    val comments by remember { derivedStateOf { viewModel.comments } }
    val totalComments = comments.sumOf { comment ->
        1 + comment.replies.size // 1 cho comment gốc + số lượng replies
    }

    // Kiểm tra trạng thái like khi component được tạo
    LaunchedEffect(postId, userId) {
        if (postId.isNotEmpty() && userId.isNotEmpty()) {
            viewModelEx.checkPostLikeStatus(postId, userId)
        }
    }
    
    // Cập nhật trạng thái like khi postLikeStatus thay đổi
    LaunchedEffect(postLikeStatus) {
        if (postId.isNotEmpty()) {
            isLiked = postLikeStatus[postId] ?: false
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

                // Các icon Like, Bookmark, Chat, Share với số liệu thực
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
                    // Hiển thị số tim từ bài Post
                    Text(text = "${post?.likesCount ?: 0}", fontSize = 12.sp, color = Color.Gray)
                }
                IconWithText(R.drawable.chat_processing_outline, "${totalComments}")
                IconWithText(R.drawable.share_all, "0") // Chưa có tính năng share
            }
        }
        if (isCommenting || comment.isNotBlank() || showEmojiPicker) {
            LaunchedEffect(isCommenting) {
                if (isCommenting) {
                    delay(200) // Đợi UI ổn định để focus
                    focusRequester.requestFocus()
                    keyboardController?.show()
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

        // Hiển thị emoji picker
        // nếu showEmojiPicker là true
        // và bàn phím đã được ẩn
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
                            // Nếu emoji rỗng, đóng emoji picker
                            showEmojiPicker = false
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } else if (emoji == "DELETE") {
                            // Nếu là lệnh xóa, xóa emoji cuối cùng
                            if (comment.isNotEmpty()) {
                                // Tìm vị trí của emoji cuối cùng bằng cách tìm ký tự Unicode cuối cùng
                                val lastEmojiIndex = comment.lastIndexOf("\\u")
                                if (lastEmojiIndex >= 0) {
                                    // Nếu tìm thấy emoji, xóa nó và các ký tự Unicode liên quan
                                    val endIndex = lastEmojiIndex + 6 // Độ dài của một emoji Unicode là 6 ký tự
                                    if (endIndex <= comment.length) {
                                        comment = comment.substring(0, lastEmojiIndex)
                                    }
                                } else {
                                    // Nếu không tìm thấy emoji, xóa ký tự cuối cùng
                                    comment = comment.dropLast(1)
                                }
                                // Đảm bảo con nháy ở cuối
                                focusRequester.requestFocus()
                            }
                        } else {
                            // Thêm emoji vào cuối text
                            comment = comment + emoji
                            // Đảm bảo con nháy ở cuối bằng cách focus lại
                            focusRequester.requestFocus()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Hàm tạo icon có số lượng bên dưới
@Composable
fun IconWithText(iconRes: Int, count: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = { /* Handle action */ }) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null
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
        // Header with close button and delete button
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
                // Nút xóa emoji cuối cùng
                IconButton(
                    onClick = { onTextAdded("DELETE") }, // Sử dụng "DELETE" làm mã đặc biệt
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Delete last emoji",
                        tint = Color.Gray
                    )
                }

                // Nút đóng emoji picker
                IconButton(
                    onClick = { onTextAdded("") }, // Empty string to close emoji picker
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

        // Emoji grid using LazyColumn with rows
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Group emojis into rows of 8
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

private const val EMOJI_COLUMNS = 10

private val emojis = listOf(
    "\ud83d\ude00", // Grinning Face
    "\ud83d\ude01", // Grinning Face With Smiling Eyes
    "\ud83d\ude02", // Face With Tears of Joy
    "\ud83d\ude03", // Smiling Face With Open Mouth
    "\ud83d\ude04", // Smiling Face With Open Mouth and Smiling Eyes
    "\ud83d\ude05", // Smiling Face With Open Mouth and Cold Sweat
    "\ud83d\ude06", // Smiling Face With Open Mouth and Tightly-Closed Eyes
    "\ud83d\ude09", // Winking Face
    "\ud83d\ude0a", // Smiling Face With Smiling Eyes
    "\ud83d\ude0b", // Face Savouring Delicious Food
    "\ud83d\ude0e", // Smiling Face With Sunglasses
    "\ud83d\ude0d", // Smiling Face With Heart-Shaped Eyes
    "\ud83d\ude18", // Face Throwing a Kiss
    "\ud83d\ude17", // Kissing Face
    "\ud83d\ude19", // Kissing Face With Smiling Eyes
    "\ud83d\ude1a", // Kissing Face With Closed Eyes
    "\u263a", // White Smiling Face
    "\ud83d\ude42", // Slightly Smiling Face
    "\ud83e\udd17", // Hugging Face
    "\ud83d\ude07", // Smiling Face With Halo
    "\ud83e\udd13", // Nerd Face
    "\ud83e\udd14", // Thinking Face
    "\ud83d\ude10", // Neutral Face
    "\ud83d\ude11", // Expressionless Face
    "\ud83d\ude36", // Face Without Mouth
    "\ud83d\ude44", // Face With Rolling Eyes
    "\ud83d\ude0f", // Smirking Face
    "\ud83d\ude23", // Persevering Face
    "\ud83d\ude25", // Disappointed but Relieved Face
    "\ud83d\ude2e", // Face With Open Mouth
    "\ud83e\udd10", // Zipper-Mouth Face
    "\ud83d\ude2f", // Hushed Face
    "\ud83d\ude2a", // Sleepy Face
    "\ud83d\ude2b", // Tired Face
    "\ud83d\ude34", // Sleeping Face
    "\ud83d\ude0c", // Relieved Face
    "\ud83d\ude1b", // Face With Stuck-Out Tongue
    "\ud83d\ude1c", // Face With Stuck-Out Tongue and Winking Eye
    "\ud83d\ude1d", // Face With Stuck-Out Tongue and Tightly-Closed Eyes
    "\ud83d\ude12", // Unamused Face
    "\ud83d\ude13", // Face With Cold Sweat
    "\ud83d\ude14", // Pensive Face
    "\ud83d\ude15", // Confused Face
    "\ud83d\ude43", // Upside-Down Face
    "\ud83e\udd11", // Money-Mouth Face
    "\ud83d\ude32", // Astonished Face
    "\ud83d\ude37", // Face With Medical Mask
    "\ud83e\udd12", // Face With Thermometer
    "\ud83e\udd15", // Face With Head-Bandage
    "\u2639", // White Frowning Face
    "\ud83d\ude41", // Slightly Frowning Face
    "\ud83d\ude16", // Confounded Face
    "\ud83d\ude1e", // Disappointed Face
    "\ud83d\ude1f", // Worried Face
    "\ud83d\ude24", // Face With Look of Triumph
    "\ud83d\ude22", // Crying Face
    "\ud83d\ude2d", // Loudly Crying Face
    "\ud83d\ude26", // Frowning Face With Open Mouth
    "\ud83d\ude27", // Anguished Face
    "\ud83d\ude28", // Fearful Face
    "\ud83d\ude29", // Weary Face
    "\ud83d\ude2c", // Grimacing Face
    "\ud83d\ude30", // Face With Open Mouth and Cold Sweat
    "\ud83d\ude31", // Face Screaming in Fear
    "\ud83d\ude33", // Flushed Face
    "\ud83d\ude35", // Dizzy Face
    "\ud83d\ude21", // Pouting Face
    "\ud83d\ude20", // Angry Face
    "\ud83d\ude08", // Smiling Face With Horns
    "\ud83d\udc7f", // Imp
    "\ud83d\udc79", // Japanese Ogre
    "\ud83d\udc7a", // Japanese Goblin
    "\ud83d\udc80", // Skull
    "\ud83d\udc7b", // Ghost
    "\ud83d\udc7d", // Extraterrestrial Alien
    "\ud83e\udd16", // Robot Face
    "\ud83d\udca9", // Pile of Poo
    "\ud83d\ude3a", // Smiling Cat Face With Open Mouth
    "\ud83d\ude38", // Grinning Cat Face With Smiling Eyes
    "\ud83d\ude39", // Cat Face With Tears of Joy
    "\ud83d\ude3b", // Smiling Cat Face With Heart-Shaped Eyes
    "\ud83d\ude3c", // Cat Face With Wry Smile
    "\ud83d\ude3d", // Kissing Cat Face With Closed Eyes
    "\ud83d\ude40", // Weary Cat Face
    "\ud83d\ude3f", // Crying Cat Face
    "\ud83d\ude3e", // Pouting Cat Face
    "\ud83d\udc66", // Boy
    "\ud83d\udc67", // Girl
    "\ud83d\udc68", // Man
    "\ud83d\udc69", // Woman
    "\ud83d\udc74", // Older Man
    "\ud83d\udc75", // Older Woman
    "\ud83d\udc76", // Baby
    "\ud83d\udc71", // Person With Blond Hair
    "\ud83d\udc6e", // Police Officer
    "\ud83d\udc72", // Man With Gua Pi Mao
    "\ud83d\udc73", // Man With Turban
    "\ud83d\udc77", // Construction Worker
    "\u26d1", // Helmet With White Cross
    "\ud83d\udc78", // Princess
    "\ud83d\udc82", // Guardsman
    "\ud83d\udd75", // Sleuth or Spy
    "\ud83c\udf85", // Father Christmas
    "\ud83d\udc70", // Bride With Veil
    "\ud83d\udc7c", // Baby Angel
    "\ud83d\udc86", // Face Massage
    "\ud83d\udc87", // Haircut
    "\ud83d\ude4d", // Person Frowning
    "\ud83d\ude4e", // Person With Pouting Face
    "\ud83d\ude45", // Face With No Good Gesture
    "\ud83d\ude46", // Face With OK Gesture
    "\ud83d\udc81", // Information Desk Person
    "\ud83d\ude4b", // Happy Person Raising One Hand
    "\ud83d\ude47", // Person Bowing Deeply
    "\ud83d\ude4c", // Person Raising Both Hands in Celebration
    "\ud83d\ude4f", // Person With Folded Hands
    "\ud83d\udde3", // Speaking Head in Silhouette
    "\ud83d\udc64", // Bust in Silhouette
    "\ud83d\udc65", // Busts in Silhouette
    "\ud83d\udeb6", // Pedestrian
    "\ud83c\udfc3", // Runner
    "\ud83d\udc6f", // Woman With Bunny Ears
    "\ud83d\udc83", // Dancer
    "\ud83d\udd74", // Man in Business Suit Levitating
    "\ud83d\udc6b", // Man and Woman Holding Hands
    "\ud83d\udc6c", // Two Men Holding Hands
    "\ud83d\udc6d", // Two Women Holding Hands
    "\ud83d\udc8f" // Kiss
)
