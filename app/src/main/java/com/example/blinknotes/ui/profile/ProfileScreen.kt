package com.example.blinknotes.ui.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.detaill.DetailScreenViewModel
import com.example.blinknotes.ui.home.FollowedScreenViewModel
import com.example.blinknotes.ui.home.LoadingAnimation
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.home.formatNumberHeart
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileScreenViewModel = viewModel(),
    guestId : String? = null
) {
    var user by remember { mutableStateOf<User?>(null) }
    var followingCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = guestId ?: currentUser?.uid
    val isOwnProfile = userId == currentUser?.uid
    var showSheet by remember { mutableStateOf(false) }
    val viewModelFollow = FollowedScreenViewModel()
    val isGuestProfile = guestId != null

    val updatedImageUrl = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("updatedImageUrl")

    val updatedCoverUrl = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("updatedCoverUrl")

    LaunchedEffect(updatedImageUrl) {
        if (updatedImageUrl != null) {
            user = user?.copy(profileImage = updatedImageUrl)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("updatedImageUrl")
        }
    }

    LaunchedEffect(updatedCoverUrl) {
        if (updatedCoverUrl != null) {
            viewModel.getCurrentUser(userId = userId.toString())  { updatedUser ->
                user = updatedUser
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("updatedCoverUrl")
        }
    }
        LaunchedEffect(userId) {
            if (userId != null) {
                viewModel.getCurrentUser(userId = userId) { fetchUser ->
                    followersCount = fetchUser!!.followers.size
                    Log.e("ProfileScreen", "Followers count: $followersCount")
                    followingCount = fetchUser!!.following.size
                    Log.e("ProfileScreen", "Following count: $followingCount")
                    Log.e("ProfileScreen", "User ID: $userId")
                    Log.e("ProfileScreen", "User: $fetchUser")
                    user = fetchUser
                }
            }
        }
    LaunchedEffect(Unit) {
        viewModelFollow.loadFollowedUsers()
    }

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            // .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxWidth(),
        topBar = {
            HeaderProfile(
                onclickMenu = {
                    navController.navigate(Screens.SettingScreenProfile.route) {
                        popUpTo(Screens.ProfileScreen.route) { inclusive = true }
                    }
                },
                isOwnProfile = !isGuestProfile,
                clickBack = {
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                TopContentProfile(
                    user = user,
                    followingCount = followingCount,
                    followersCount = followersCount,
                    onProfileImageClick = {
                        val encodedUrl = URLEncoder.encode(user?.profileImage ?: "", StandardCharsets.UTF_8.toString())
                        val userId = user?.userId ?: ""
                        navController.navigate("edit_profile_image_screen/$encodedUrl/$userId")
                    },
                    onCoverImageClick = {
                        val encodedUrl = URLEncoder.encode(user?.coverImage ?: "", StandardCharsets.UTF_8.toString())
                        navController.navigate("edit_cover_image_screen/$encodedUrl")
                    },
                    showEditProfile = { showSheet = it },
                    navController = navController,
                    isOwnProfile = !isGuestProfile,
                    idUserOfPost =  userId ?: "",
                    userId =currentUser?.uid ?: "",
                    userLinkId = userId?:"",
                )
            }

            item {
                TabContentProfile(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            if (isOwnProfile) {
                when (selectedTab) {
                    0 -> item {
                        TabMyPost(navController)
                    }
                    1 -> item { TabMySavePost() }
                    2 -> item { TabMyHeartPost(navController) }
                }
            }
        }

        // Show Edit Profile Bottom Sheet
        if (showSheet) {
            EditProfileBottomSheet(
                currentUsername = user?.username ?: "",
                currentBlinkNotesId = user?.blinkNotesId ?: "",
                currentBio = user?.bio ?: "",
                onDismiss = { showSheet = false },
                onSave = { username, blinkNotesId, bio ->
                    currentUser?.uid?.let { userId ->
                        viewModel.updateProfile(userId, username, blinkNotesId, bio)
                        viewModel.getCurrentUser( userId = userId) { updatedUser ->
                            user = updatedUser
                        }
                    }
                    showSheet = false
                }
            )
        }
    }
}

@Composable
fun HeaderProfile(
    onclickMenu: () -> Unit,
    isOwnProfile: Boolean = true,
    clickBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(!isOwnProfile) {
            IconButton(onClick = {
                clickBack()
            }) {
                Image(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {}) {
            Image(
                painter = painterResource(R.drawable.share_all),
                contentDescription = "Share profile",
                modifier = Modifier.size(24.dp)
            )
        }
        if(isOwnProfile) {
            IconButton(onClick = {
                onclickMenu()
            }) {
                Image(
                    painter = painterResource(R.drawable.reorder_horizontal),
                    contentDescription = "Reorder",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopContentProfile(
    user: User?,
    followingCount: Int,
    followersCount: Int,
    onProfileImageClick: () -> Unit,
    onCoverImageClick: () -> Unit,
    showEditProfile: (Boolean) -> Unit,
    navController: NavHostController,
    isOwnProfile: Boolean,
    viewModel: DetailScreenViewModel = viewModel(),
    idUserOfPost: String,
    userId: String = "",
    userLinkId: String =""
    ) {
    val followStatus by viewModel.followStatus.collectAsState()
    val currentStatus = followStatus[idUserOfPost] ?: DetailScreenViewModel.FollowStatus()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val viewModelProfile : ProfileScreenViewModel = viewModel()
    val userLink by viewModelProfile.user.collectAsState()
    LaunchedEffect(userLinkId) {
        if (userLinkId != null) {
            viewModelProfile.fetchUser(userLinkId)
        }
    }
    LaunchedEffect(idUserOfPost) {
        if (userId.isNotEmpty() && idUserOfPost.isNotEmpty()) {
            viewModel.checkFollowStatus(userId, idUserOfPost)
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cover Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
                    .clickable {
                        // Navigate to ViewCoverImageScreen
                        user?.coverImage?.let { coverImage ->
                            val encodedUrl = URLEncoder.encode(coverImage, StandardCharsets.UTF_8.toString())
                            Log.d("ProfileScreen", "Cover Image URL before encode: $coverImage")
                            Log.d("ProfileScreen", "Cover Image URL after encode: $encodedUrl")
                            navController.navigate(Screens.ViewCoverImageScreen.route.replace("{imageUrl}", encodedUrl))
                        }
                    }
            ) {
                AsyncImage(
                    model = user?.coverImage,
                    contentDescription = "Cover Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )
            }

            if (isOwnProfile) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(36.dp) // Tăng nhẹ kích thước để dễ chạm hơn
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCoverImageClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Edit Cover",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Profile Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    AsyncImage(
                        model = user?.profileImage,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onProfileImageClick
                            ),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.accounticon),
                        fallback = painterResource(id = R.drawable.accounticon)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user?.username ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = user?.blinkNotesId ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$followingCount",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Đang Follow",
                            color = Color.Gray,
                            modifier = Modifier
                                .clickable {
                                    // Navigate to FollowedScreen
                                    navController.navigate(Screens.FollowingAndFollowerScreen.route + "/${user?.userId}")
                                }
                        )
                        Text(
                            text = "$followersCount",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Follower",
                            color = Color.Gray,
                            modifier = Modifier
                                .clickable {
                                    // Navigate to FollowerScreen
                                    navController.navigate(Screens.FollowingAndFollowerScreen.route + "/${user?.userId}")
                                }
                        )
                    }
                }
            }

            if (!user?.bio.isNullOrEmpty()) {
                Text(
                    text = user?.bio ?: "",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            SocialMediaLinks(user = userLink ?: User())


            if (isOwnProfile) {
                Button(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.gainsboro)),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { showEditProfile(true) }
                ) {
                    Text(
                        text = "Sửa hồ sơ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            } else {
                Button(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f),
                    colors = if (currentStatus.isFollowing) ButtonDefaults.buttonColors(containerColor = colorResource(R.color.gainsboro))
                    else ButtonDefaults.buttonColors(containerColor = colorResource(R.color.tab_line)),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        if (currentStatus.isFollowing) {
                            showSheet = true
                        } else {
                            viewModel.toggleFollow(userId, idUserOfPost)
                        }
                    /* Handle follow/unfollow action */ }
                ) {
                    val buttonText = when {
                        currentStatus.isFollowing && currentStatus.isFollowedBy -> "Bạn bè"
                        currentStatus.isFollowing -> "Đang Follow"
                        else -> "Follow"
                    }

                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
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
        }
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
                        ) {
                            if (userId.isNotEmpty() && userId.isNotEmpty()) {
                                viewModel.toggleFollow(userId, idUserOfPost)
                            }
                            showSheet = false
                        },
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

@Composable
fun TabContentProfile(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        R.drawable.table,
        R.drawable.book_lock_outline,
        R.drawable.heart_off
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, iconRes ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onTabSelected(index)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (index == selectedTab) Color.Black else Color.Gray
                        )
                    }
                    // Indicator line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                color = if (index == selectedTab) Color.Black else Color.Transparent
                            )
                    )
                }
            }
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabMyPost(navController: NavController
) {
    val viewModel: ProfileScreenViewModel = viewModel()
    val viewModelUser: DetailScreenViewModel = viewModel()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            viewModel.getUserPosts(userId) { fetchedPosts ->
                posts = fetchedPosts
                isLoading = false
            }
            viewModel.loadDrafts(userId)
        } ?: run { isLoading = false }
    }
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            viewModelUser.getUserById(userId) { fetchedUser ->
                user = fetchedUser
                isLoading = false
            }
        } ?: run { isLoading = false }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingAnimation()
        }
    } else if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có bài viết nào",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            posts.forEach { post ->
                ItemsTabMyPost(
                    imageLink = post.firstImageUrl,
                    numberHeart = post.likesCount,
                    profileImage = user!!.profileImage,
                    userName = user!!.username,
                    isDraft = true,
                    onclick = {
                        navController.navigate("details/${post.id}/${post.userId}")
                    },
                    onLongPress = {
                        selectedPostId = post.id
                        showDeleteSheet = true

                    },
                    onDelete = {
                        viewModel.deletePost(post.id) {
                            posts = posts.filter { it.id != post.id }
                        }
                    },
                    modifier = Modifier
                        .width((LocalConfiguration.current.screenWidthDp.dp - 24.dp) / 2)
                )
            }
            viewModel.drafts.forEach { draft ->
                ItemsTabMyPost(
                    imageLink = draft.firstImageUrl,
                    numberHeart = null,
                    profileImage = user!!.profileImage,
                    userName = "Nháp",
                    isDraft = false,
                    onclick = {
                        navController.navigate("add_photo_screen?draftId=${draft.id}")
                    },
                    onLongPress = {
                        selectedPostId = draft.id
                        showDeleteSheet = true
                    },
                    onDelete = {
                        viewModel.deletePost(draft.id) {
                            posts = posts.filter { it.id != draft.id }
                        }
                    },
                    modifier = Modifier
                        .width((LocalConfiguration.current.screenWidthDp.dp - 24.dp) / 2)
                )
            }
        }
    }

    if (showDeleteSheet) {
        DeletePostBottomSheet(
            onDismiss = { showDeleteSheet = false },
            onDelete = {
                selectedPostId?.let { postId ->
                    viewModel.deletePost(postId) {
                        posts = posts.filter { it.id != postId }
                        showDeleteSheet = false
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletePostBottomSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier =
        Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        contentColor = Color.Black,
        tonalElevation = 8.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Xóa bài viết?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bạn có chắc chắn muốn xóa bài viết này không?",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text(text = "Hủy", color = Color.Black)
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(text = "Xóa", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TabMySavePost() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Search Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabMyHeartPost(navController: NavController) {
    val viewModel: ProfileScreenViewModel = viewModel()
    val viewModelUser: DetailScreenViewModel = viewModel()
   // var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
   // var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseAuth.getInstance().currentUser
   // var user by remember { mutableStateOf<User?>(null) }

    val likedPosts = viewModel.postsWithUsers // state list trong ViewModel

    // Gọi khi màn hình được mở lần đầu
    LaunchedEffect(Unit) {
        viewModel.loadLikedPosts(currentUser?.uid ?: "")
        isLoading = false
    }


    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingAnimation()
        }
    } else if (likedPosts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có bài viết nào đã thả tim",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            likedPosts.forEach { likePost ->
                ItemsTabMyPost(
                    imageLink = likePost.post.firstImageUrl,
                    numberHeart = likePost.post.likesCount,
                    profileImage = likePost.user?.profileImage,
                    isDraft = true,
                    userName = likePost.user?.username ?: "",
                    onclick = {
                        navController.navigate("details/${likePost.post.id}/${likePost.user?.userId ?: likePost.post.userIdCmt}")
                    },
                    onLongPress = {
                        // Handle long press if needed
                    },
                    onDelete = {
//                        viewModel.deletePost(likePost.post.id) {
//                            // Handle successful deletion
//                        }
                    },
                    modifier = Modifier
                        .width((LocalConfiguration.current.screenWidthDp.dp - 24.dp) / 2) // Calculate width based on screen size minus padding
                )
            }
        }
    }
}

@Composable
fun ItemsTabMyPost(
    imageLink: String?,
    numberHeart: Int?,
    profileImage: String?,
    userName: String,
    modifier: Modifier = Modifier,
    onclick: () -> Unit,
    onDelete: () -> Unit,
    isDraft: Boolean = false,
    onLongPress: () -> Unit ,
) {
    val formattedTextnumberHeart = formatNumberHeart(numberHeart)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .combinedClickable(
                onClick = onclick,
                onLongClick = onLongPress
            )
            .padding(4.dp)
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(R.color.turquoise)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model =  imageLink,
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if(!isDraft) {
                Text(
                    text = "Nháp",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // User info section with fixed width
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profileImage,
                    contentScale = ContentScale.Crop,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.accounticon),
                    error = painterResource(id = R.drawable.accounticon)
                )

                Text(
                    text = userName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Heart count and eye icon section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = formattedTextnumberHeart,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.black),
                    modifier = Modifier.padding(end = 2.dp)
                )

                IconButton(onClick = { onDelete() }) {
                    Icon(
                        painter = painterResource(R.drawable.eye_outline),
                        contentDescription = "View Post",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SocialMediaLinks(user: User) {
    val context = LocalContext.current

    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
        user.facebookLink?.let { facebookLink ->
            if (facebookLink.isNotEmpty()) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.facebook),
                    contentDescription = "Facebook",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            openLink(context, facebookLink)
                        },
                    tint = Color(0xFF1877F2)
                )
            }
        }
        user.instagramLink?.let { instagramLink ->
            if (instagramLink.isNotEmpty()) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.instagram),
                    contentDescription = "Instagram",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            openLink(context, instagramLink)
                        },
                    tint = Color(0xFFE4405F)
                )
            }
        }
        user.twitterLink?.let { twitterLink ->
            if (twitterLink.isNotEmpty()) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.twitter),
                    contentDescription = "Twitter",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            openLink(context, twitterLink)
                        },
                    tint = Color(0xFF1DA1F2)
                )
            }
        }
    }
}

fun openLink(context: android.content.Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Log.e("openLink", "No application can handle this request: $url")
            // Fallback: Open in a browser
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        Log.e("openLink", "Error opening link: $url", e)
    }
}