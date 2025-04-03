package com.example.blinknotes.ui.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.detaill.DetailScreenViewModel
import com.example.blinknotes.ui.home.LoadingAnimation
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.home.formatNumberHeart
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    var userSignedIn by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    userSignedIn = currentUser != null
    val viewModel: ProfileScreenViewModel = viewModel()
    var user by remember { mutableStateOf<User?>(null) }
    var followingCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    var showEditProfile by remember { mutableStateOf(false) }

    // Lắng nghe kết quả từ EditProfileImageScreen và EditCoverImageScreen
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
            viewModel.getCurrentUser { updatedUser ->
                user = updatedUser
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("updatedCoverUrl")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getCurrentUser { fetchedUser ->
            user = fetchedUser
            if (fetchedUser != null) {
                viewModel.getFollowCounts(fetchedUser.userId) { following, followers ->
                    followingCount = following
                    followersCount = followers
                }
            }
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                        navController.navigate("edit_profile_image_screen/$encodedUrl")
                    },
                    onCoverImageClick = {
                        val encodedUrl = URLEncoder.encode(user?.coverImage ?: "", StandardCharsets.UTF_8.toString())
                        navController.navigate("edit_cover_image_screen/$encodedUrl")
                    },
                    showEditProfile = { showEditProfile = it },
                    navController = navController
                )
            }

            item {
                TabContentProfile(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            if (userSignedIn) {
                when (selectedTab) {
                    0 -> item {
                        TabMyPost(navController)
                    }
                    1 -> item { TabMySavePost() }
                    2 -> item { TabMyHeartPost() }
                }
            }
        }

        // Show Edit Profile Bottom Sheet
        if (showEditProfile) {
            EditProfileBottomSheet(
                currentUsername = user?.username ?: "",
                currentBlinkNotesId = user?.blinkNotesId ?: "",
                currentBio = user?.bio ?: "",
                onDismiss = { showEditProfile = false },
                onSave = { username, blinkNotesId, bio ->
                    currentUser?.uid?.let { userId ->
                        viewModel.updateProfile(userId, username, blinkNotesId, bio)
                        viewModel.getCurrentUser { updatedUser ->
                            user = updatedUser
                        }
                    }
                    showEditProfile = false
                }
            )
        }
    }
}

@Composable
fun HeaderProfile(
    onclickMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {}) {
            Image(
                painter = painterResource(R.drawable.share_all),
                contentDescription = "Share profile",
                modifier = Modifier.size(24.dp)
            )
        }

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

@Composable
fun TopContentProfile(
    user: User?,
    followingCount: Int,
    followersCount: Int,
    onProfileImageClick: () -> Unit,
    onCoverImageClick: () -> Unit,
    showEditProfile: (Boolean) -> Unit,
    navController: NavHostController
) {
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
            }

            // Camera button for updating cover image
            IconButton(
                onClick = onCoverImageClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Edit Cover",
                    tint = Color.White
                )
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
                            color = Color.Gray
                        )
                        Text(
                            text = "$followersCount",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Follower",
                            color = Color.Gray
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(navController: NavController, scrollBehavior: TopAppBarScrollBehavior) {

    val collapsedFraction = scrollBehavior.state.collapsedFraction

    MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.lightgray),
            titleContentColor = colorResource(R.color.black),
        ),
        title = {
            if (collapsedFraction == 1f) {
                // Khi THU NHỎ hoàn toàn -> Chỉ hiển thị 1 Icon + 1 Text
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo_app),
                        contentDescription = "Collapse Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                       text =  "User Name",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Khi MỞ RỘNG -> Hiển thị nhiều nội dung trong một Composable
           //  TopContentProfile()
            }
        },
        navigationIcon = {
//            Image(
//                painter = painterResource(R.drawable.icon_back),
//                contentDescription = "",
//                modifier = Modifier
//                    .size(24.dp)
//                    .clickable {
//                        navController.popBackStack()
//                    }
//            )
        },
        actions = {
//            HeaderProfile (
//                onclickMenu = {
//                    navController.navigate(Screens.SettingScreenProfile.route) {
//                        popUpTo(Screens.ProfileScreen.route) { inclusive = true }
//                    }
//                }
//            )
        },
        scrollBehavior = scrollBehavior
    )
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
fun TabMyPost(navController: NavController) {
    val viewModel: ProfileScreenViewModel = viewModel()
    val viewModelUser: DetailScreenViewModel = viewModel()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var  user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            viewModel.getUserPosts(userId) { fetchedPosts ->
                posts = fetchedPosts
                isLoading = false
            }
        } ?: run { isLoading = false }
    }
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            viewModelUser.getUserById (userId) { fetchedUser ->
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
                    onclick = {
                        //navController.navigate("details/${post.postId}/${post.userId}")
                    },
                    modifier = Modifier
                        .weight(1f) // Đảm bảo item chia đều không gian
                        .fillMaxWidth(0.5f),
                )
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

@Composable
fun TabMyHeartPost() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Profile Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ItemsTabMyPost(
    imageLink: String?,
    numberHeart: Int?,
    profileImage: String?,
    userName: String,
    modifier: Modifier = Modifier,
    onclick: () -> Unit
) {
    val formattedTextnumberHeart = formatNumberHeart(numberHeart)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onclick() }
            .padding(4.dp)
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        ) {
            AsyncImage(
                model = imageLink,
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
//                placeholder = painterResource(id = R.drawable.placeholder_image),
//                error = painterResource(id = R.drawable.placeholder_image)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profileImage,
                    contentScale = ContentScale.Crop,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(20.dp)
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
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Text(
                text = formattedTextnumberHeart,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.black),
                modifier = Modifier.padding(end = 4.dp)
            )
            Image(
                painter = painterResource(R.drawable.eye_outline),
                contentDescription = null,
                Modifier
                    .size(20.dp),
            )
        }
    }
}

