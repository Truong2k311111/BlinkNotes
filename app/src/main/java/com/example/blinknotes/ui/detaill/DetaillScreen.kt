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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetaillScreen(navController: NavController,
                  viewModel: ExploreScreenViewModel = viewModel(),
                  postId: String,
                  userId:String,
                  viewModelDetail: DetailScreenViewModel = viewModel(),
                  ) {
    var user by remember { mutableStateOf<User?>(null) }

    var post by remember { mutableStateOf<Post?>(null) }
    val scrollState = rememberScrollState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userIdCmt = currentUser?.uid
    val comments by remember { derivedStateOf { viewModelDetail.comments } }
    val users by viewModel.users.collectAsState()
    LaunchedEffect(postId) {
        Log.d("DetailScreen", "Fetching post with ID: $postId")
        viewModel.getPostByPostId(postId) { fetchedPost ->
            post = fetchedPost
            if (fetchedPost != null) {
                Log.d("DetailScreen", "Post fetched: $fetchedPost")
                Log.d("DetailScreen", "Fetching user with ID: ${fetchedPost.userId}")

                // Khi có bài post, lấy user theo userId
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
    Scaffold(

        bottomBar = {
            Divider()
            BottomBarDetail(
                postId = post?.id ?: "",
                userId = userIdCmt.toString(),
                parentCommentId = userIdCmt.toString(),
                navController = navController
            )
                    },
        topBar = {
            HeaderDetaill(
                navController = navController,
                username = user?.username ?: "Loading...",
                profileImage = user?.profileImage ?: ""
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
    caption:String,
    content:String,
    time:String,
){

    val pagerState = rememberPagerState(pageCount = {pageCount })
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        HorizontalPager(state = pagerState,
            modifier = Modifier
                .background(color = colorResource(id = R.color.cornsilk))
        ) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp)
            )
        }
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size( 6.dp)
                        .background(
                            if (index == pagerState.currentPage) Color.Black else Color.Gray,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = caption,
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(start = 12.dp))
            Text(
                text = time,
                fontSize = 13.sp,
                color = colorResource(R.color.bgr),
                modifier = Modifier
                    .padding(start = 12.dp
                    , top = 12.dp, bottom = 12.dp))

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
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                if (isAuthor) {
                    Row {
                        Text(
                            text = "$userName * ",
                            fontSize = 13.sp,
                            color = colorResource(R.color.bgr)
                        )
                        Text(
                            text = "Tác giả",
                            fontSize = 13.sp,
                            color = colorResource(R.color.tab_line)
                        )
                    }
                } else {
                    if (isReply && parentUserInfo != null) {
                        // Nếu là reply, hiển thị "username > username cha"
                        Log.d("CommentItems", "Displaying reply: $userName > ${parentUserInfo?.username}")
                        Text(
                            text = "$userName > ${parentUserInfo?.username}",
                            fontSize = 13.sp,
                            color = colorResource(R.color.bgr)
                        )
                    } else {
                        Log.d("CommentItems", "Not displaying reply info: isReply=$isReply, parentUserInfo=${parentUserInfo != null}")
                    Text(
                        text = userName,
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
                            .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(24.dp))
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
@Composable
fun HeaderDetaill( navController: NavController, profileImage: String, username: String){
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
            )

            Text(
                text = username,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.teal_200)),
            shape = RoundedCornerShape(24.dp),
            onClick = { /* Thêm logic Follow */ }
        ) {
            Text(
                text = "Follow",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        IconButton(
            onClick = { /* Thêm logic Search */ },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBarDetail(
    postId: String,
    userId: String,
    parentCommentId: String,
    navController: NavController
) {
    var comment by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: DetailScreenViewModel = viewModel()

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
                value = comment,
                onValueChange = { comment = it },
                        placeholder = { Text("Viết bình luận...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                            ) },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(24.dp))
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
                    if (comment.isNotBlank()) {
                        // Nếu parentCommentId là userId, nghĩa là đang tạo comment mới
                        val actualParentId = if (parentCommentId == userId) null else parentCommentId
                        viewModel.addComment(postId, userId, comment, actualParentId)
                        comment = ""
                        keyboardController?.hide()
                    }
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
            }
        }
    }
}
