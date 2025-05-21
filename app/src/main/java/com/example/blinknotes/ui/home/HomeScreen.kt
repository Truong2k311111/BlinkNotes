package com.example.blinknotes.ui.home

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.IconButton
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.Auth.images
import com.example.blinknotes.ui.eventClick.handleClick
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewmodel: HomeScreenViewModel,
) {
    var userSignedIn by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    userSignedIn = currentUser != null
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { if (userSignedIn) 2 else 1 }
    )
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxWidth()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            TabContent(
                pagerState = pagerState,
                scope = scope,
                selectedTab = selectedTab,
                onTabSelected = { tabIndex -> selectedTab = tabIndex },
                onSearchClick = {
                    navController.navigate(Screens.SearchScreen.route)
                }
            )
            if (userSignedIn) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                            0 -> ExploreScreen(
                                navController = navController,
                                userIdLogin = currentUser!!.uid
                            )
                            1 -> FollowedScreen(
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreScreen(
    navController: NavController,
    userIdLogin: String,
    viewModel: ExploreScreenViewModel = viewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val gridState = rememberLazyStaggeredGridState()

    // Handle pull-to-refresh
    LaunchedEffect(gridState) {
        snapshotFlow {
            gridState.firstVisibleItemIndex == 0 &&
                    gridState.firstVisibleItemScrollOffset < -100
        }.collect { shouldRefresh ->
            if (shouldRefresh && !isRefreshing) {
                viewModel.refresh()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ImageListItem(
                navController = navController,
                userId = userIdLogin,
                viewModel = viewModel
            )

            // Show refresh indicator at the top
            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
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
    }
}

@Composable
fun TabContent(pagerState: PagerState, scope: CoroutineScope, onSearchClick: () -> Unit,  selectedTab: Int,
               onTabSelected: (Int) -> Unit,){
    val tabs = listOf("Đề xuất", "Đã Follow")
    val animatableOffset = remember { Animatable(0f) }

    LaunchedEffect(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        val target = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * 5f
        animatableOffset.animateTo(
            target,
            animationSpec = tween(
                durationMillis = 500, // 👈 chỉnh thời gian dài hơn để di chuyển chậm
                easing = LinearOutSlowInEasing
            )
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,

            ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable (
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ){
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            onTabSelected(index)

                        }

                ) {
                    Text(
                        text = title,
                        color = if (index == selectedTab) Color.Black else Color.Gray,
                        fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 22.sp
                    )
                    if (index == selectedTab) {
                        WavyLineBox(
                            modifier = Modifier
                                .offset {
                                    IntOffset(animatableOffset.value.roundToInt(),0)
                                },
                            color = Color.Cyan
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(50.dp)
        ) {

            Icon(
                painter = painterResource(R.drawable.icon_search),
                contentDescription = "Search",
                tint = Color.Black
            )
        }
    }
}
@Composable
fun WavyLineBox(
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan
) {
    Canvas(
        modifier = modifier
            .size(width = 100.dp, height = 10.dp) // ngang dài hơn dọc
    ) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            // Bắt đầu từ trái
            moveTo(0f, height * 0.45f)
            // Lên chéo phải
            lineTo(width * 0.45f, height * 0.35f)
            // Lên gấp góc trên
            lineTo(width * 0.45f, height * 0.15f)
            // Qua phải dưới
            lineTo(width, height * 0.55f)
            // Xuống giữa
            lineTo(width * 0.55f, height * 0.65f)
            // Xuống đáy
            lineTo(width * 0.55f, height * 0.85f)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Color.Cyan, Color.Blue, Color(0xFF000080)),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
        )
    }
}

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    circleSize: Dp = 12.dp,
    circleColor: Color = Color.Magenta,
    spaceBetween: Dp = 5.dp,
    travelDistance: Dp = 10.dp
) {
    val circles = listOf(
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) }
    )

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(key1 = animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 with LinearOutSlowInEasing
                        1.0f at 300 with LinearOutSlowInEasing
                        0.0f at 600 with LinearOutSlowInEasing
                        0.0f at 1200 with LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    val circleValues = circles.map { it.value }
    val distance = with(LocalDensity.current) { travelDistance.toPx() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween)
    ) {
        circleValues.forEach { value ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .graphicsLayer {
                        translationY = -value * distance
                    }
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    )
            )
        }
    }

}
