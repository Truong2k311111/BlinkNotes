package com.example.blinknotes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.detaill.DetailScreenViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FollowedScreen(
    navController: NavController,
    viewModel: FollowedScreenViewModel = viewModel()
) {
    val followedUsers by viewModel.followedUsers.collectAsState()
    val suggestedUsers by viewModel.suggestedUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelDetail = DetailScreenViewModel()
    val viewModelEx = ExploreScreenViewModel()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val userIdListFollowed = followedUsers.map { it.userId }
    val users by viewModelEx.users.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val spacing = 8.dp * 3
    val itemWidth = (screenWidth - spacing) / 2

    LaunchedEffect(Unit) {
        viewModel.loadFollowedUsers()
        viewModel.loadSuggestedUsers()
    }
    LaunchedEffect(userIdListFollowed) {
        viewModelDetail.getPostsByLargeUserList(userIdListFollowed) { result ->
            posts = result
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(
                    circleSize = 8.dp,
                    circleColor = Color(0xFF005BEA),
                    spaceBetween = 4.dp,
                    travelDistance = 6.dp
                )
            }
        } else if (followedUsers.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Bạn chưa follow ai",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Follow người dùng để xem bài viết của họ",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                if (suggestedUsers.isNotEmpty()) {
                    item {
                        Text(
                            text = "Đề xuất cho bạn",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(suggestedUsers) { user ->
                        SuggestedUserItem(
                            user = user,
                            onUserClick = {
                                val encodedUserId = URLEncoder.encode(user.userId, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screens.ProfileScreen.route + "/$encodedUserId")
                            },
                            onFollowClick = {
                                viewModel.followUser(user.userId)
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        maxItemsInEachRow = 2,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        posts.forEach { post ->
                            LaunchedEffect(post.userId) {
                                if (!users.containsKey(post.userId)) {
                                    viewModelEx.fetchUser(post.userId)
                                }
                                viewModelEx.checkPostLikeStatus(post.id, post.userId)
                            }
                            val user = users[post.userId]
                            val isFavorite by remember(post.id) {
                                derivedStateOf {
                                    viewModelEx.postLikeStatus.value[post.id] ?: false
                                }
                            }

                            ItemsFeedFolow(
                                imageLink = post.firstImageUrl,
                                userName = user?.username ?: "Loading...",
                                profileImage = user?.profileImage ?: "",
                                numberHeart = post.likesCount,
                                status = post.caption,
                                isFavorite = isFavorite,
                                modifier = Modifier
                                    .width( itemWidth),
                                onclick = {
                                    viewModelEx.getPostByPostId(post.id) { post ->
                                        post?.let {
                                            navController.navigate(Screens.DetaillScreen.route + "/${it.id}/${post.userId}")
                                        }
                                    }
                                },
                                onLikeClick = {
                                    viewModelEx.togglePostLike(post.id, post.userId)
                                },

                            )

                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Bạn đã xem hết",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Follow nhiều người hơn hoặc khám phá thêm bài đăng trên bản tin 'Đề xuất' của bạn.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                items(followedUsers) { user ->
                    FollowedUserItem(
                        user = user,
                        onUserClick = {
                            navController.navigate(
                                Screens.ProfileScreen.route + "/${user.userId}"
                            )
                        },
                        onPostClick = { postId -> 
                            navController.navigate(Screens.DetaillScreen.route + "/$postId")
                        },
                        onFollowClick = { viewModel.unfollowUser(user.userId) },
                        isFollowing = true
                    )
                }
                if (suggestedUsers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Gợi ý",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(suggestedUsers) { user ->
                        SuggestedUserItem(
                            user = user,
                            onUserClick = {
                                val encodedUserId = URLEncoder.encode(user.userId, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screens.ProfileScreen.route + "/$encodedUserId")
                            },
                            onFollowClick = {
                                viewModel.followUser(user.userId)
                            }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FollowedUserItem(
    user: User,
    onUserClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    isFollowing: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onUserClick
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profileImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = user.username,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Button(
                    onClick = onFollowClick,
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.bgr)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (user.recentPost.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

            }
        }
    }
}

@Composable
fun SuggestedUserItem(
    user: User,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onUserClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profileImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.username,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${user.followersCount} người theo dõi",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF005BEA)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Follow",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Follow",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
@Composable
fun ItemsFeedFolow(
    imageLink: String?,
    numberHeart: Int?,
    status: String?,
    profileImage: String,
    userName: String,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onclick: () -> Unit,
    onLikeClick: () -> Unit
) {
    var imageSize by remember { mutableStateOf(Size.Zero) }
    var isFavoriteState by remember { mutableStateOf(isFavorite) }
    var currentLikes by remember { mutableStateOf(numberHeart ?: 0) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isFavorite, numberHeart) {
        isFavoriteState = isFavorite
        currentLikes = numberHeart ?: 0
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                onclick()
            }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(end = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = imageLink,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .onGloballyPositioned { layoutCoordinates ->
                        val width = layoutCoordinates.size.width.toFloat()
                        val height = layoutCoordinates.size.height.toFloat()
                        imageSize = Size(width, height)
                    },
            )
            Text(
                text = status.toString(),
                modifier = Modifier
                    .padding(8.dp)
                    .align(alignment = Alignment.Start),
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, start = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = profileImage,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(id = R.drawable.accounticon),
                    )

                    Text(
                        text = userName,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatNumberHeart(currentLikes),
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = if (isFavoriteState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = if (isFavoriteState) Color.Red else Color.Gray,
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                isFavoriteState = !isFavoriteState
                                onLikeClick()
                            }
                    )
                }
            }
        }
    }
}