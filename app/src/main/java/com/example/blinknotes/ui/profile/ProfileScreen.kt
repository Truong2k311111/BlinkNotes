package com.example.blinknotes.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.blinknotes.ui.home.User
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

    // Lắng nghe kết quả từ EditProfileImageScreen
    val updatedImageUrl = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("updatedImageUrl")
    
    LaunchedEffect(updatedImageUrl) {
        if (updatedImageUrl != null) {
            // Cập nhật UI với URL mới
            user = user?.copy(profileImage = updatedImageUrl)
            // Xóa giá trị đã sử dụng
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("updatedImageUrl")
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

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { if (userSignedIn) 3 else 0 }
    )
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
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
        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            TopContentProfile(
                user = user,
                followingCount = followingCount,
                followersCount = followersCount,
                onProfileImageClick = {
                    // Navigate to EditProfileImageScreen với currentImageUrl
                    val encodedUrl = URLEncoder.encode(user?.profileImage ?: "", StandardCharsets.UTF_8.toString())
                    navController.navigate("edit_profile_image_screen/$encodedUrl")
                }
            )
            TabContentProfile(
                pagerState = pagerState,
                scope = scope,
                selectedTab = selectedTab,
                onTabSelected = { tabIndex -> selectedTab = tabIndex },
            )

            if (userSignedIn) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (page) {
                        0 -> HomeScreen1()
                        1 -> SearchScreen1()
                        2 -> ProfileScreen1()
                    }
                }
            }
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
    onProfileImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = user?.profileImage ?: R.drawable.logo_app,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(90.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onProfileImageClick
                    )
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = user?.username ?: "Loading...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.username ?: "Loading...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$followingCount", fontWeight = FontWeight.Bold)
                    Text(text = "Đang Follow", color = Color.Gray)
                    Text(text = "$followersCount", fontWeight = FontWeight.Bold)
                    Text(text = "Follower", color = Color.Gray)
                }
            }
        }

        Button(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(24.dp),
            onClick = { }
        ) {
            Text(
                text = "Sửa hồ sơ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )
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
    pagerState: PagerState,
    scope: CoroutineScope,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        R.drawable.share_all,
        R.drawable.share_all,
        R.drawable.share_all
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, iconRes ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (index == selectedTab) Color.Black else Color.Gray
                    )
                }
            }
        }
       Divider(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun HomeScreen1() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchScreen1() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Search Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileScreen1() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Profile Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun Preview(){

}