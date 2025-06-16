package com.example.blinknotes.presentation.ui.Screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.components.LoadingAnimation
import com.example.blinknotes.presentation.viewModel.ExploreScreenViewModel
import com.example.blinknotes.utils.formatNumberHeart
import com.google.firebase.firestore.FirebaseFirestore
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageListItem(
    navController: NavController,
    userId: String
) {

    val viewModel: ExploreScreenViewModel = hiltViewModel()
    val posts by viewModel.posts.collectAsState()
    val users by viewModel.users.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val imageListState = rememberLazyStaggeredGridState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    LaunchedEffect(imageListState) {
        snapshotFlow { imageListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 2 && !viewModel.isLoading) {
                    viewModel.loadMorePosts()
                }
            }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyVerticalStaggeredGrid(
            state = imageListState,
            flingBehavior = ScrollableDefaults.flingBehavior(),
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 1.dp,
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            content = {
                items(posts) { post ->
                    LaunchedEffect(post.userId) {
                        if (!users.containsKey(post.userId)) {
                            viewModel.fetchUser(post.userId)
                        }
                        viewModel.checkPostLikeStatus(post.id, userId)
                    }
                    val user = users[post.userId]
                    val isFavorite by remember(post.id) {
                        derivedStateOf {
                            viewModel.postLikeStatus.value[post.id] ?: false
                        }
                    }
                    ItemsFeed(
                        imageLink = post.imageUrls.firstOrNull(),
                        userName = user?.username ?: "Loading...",
                        profileImage = user?.profileImage ?: "",
                        numberHeart = post.likesCount,
                        status = post.caption,
                        isFavorite = isFavorite,
                        modifier = Modifier,
                        onclick = {
                            viewModel.getPostByPostId(post.id) { post ->
                                post?.let {
                                    navController.navigate(Screens.DetaillScreen.route + "/${it.id}/${userId}")
                                }
                            }
                        },
                        onLikeClick = {
                            viewModel.toggleLike(post.id, userId)
                        }
                    )
                }
                item(span = StaggeredGridItemSpan.FullLine) {
                    if (viewModel.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingAnimation(
                                circleSize = 8.dp,
                                circleColor = Color(0xFF005BEA),
                                spaceBetween = 4.dp,
                                travelDistance = 6.dp
                            )
                        }
                    }
                }
            },
            modifier = Modifier
        )
    }
}
@Composable
fun ItemsFeed(
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
        modifier = Modifier
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


