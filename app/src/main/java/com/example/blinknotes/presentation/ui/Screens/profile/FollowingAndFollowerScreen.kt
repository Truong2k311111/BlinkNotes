package com.example.blinknotes.presentation.ui.Screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.blinknotes.R
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.viewModel.FollowedScreenViewModel
import com.example.blinknotes.presentation.viewModel.ProfileScreenViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FollowingAndFollowerScreen(
    navController: NavController,
    viewModelPro: ProfileScreenViewModel = hiltViewModel(),
    userId: String
) {
    val viewModel: FollowedScreenViewModel = hiltViewModel()

    val followedUsers by viewModel.followedUsers.collectAsState()
    val followersUsers by viewModel.followersUsers.collectAsState(initial = emptyList())
    var user by remember { mutableStateOf<User?>(null) }
    var followingCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = userId
    val isOwnProfile = userId == currentUser?.uid
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFollowedUsers()
        viewModel.loadSuggestedUsers()
    }
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.setSelectedUserId(userId)
            viewModelPro.getCurrentUser(userId = userId) { fetchUser ->
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
        viewModel.loadFollowedUsers()
    }
        val tabTitles = listOf(
            "Đang follow (${followingCount})",
            "Follower (${followersCount})"
        )
        var selectedTabIndex by remember { mutableStateOf(0) }
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            viewModel.loadFollowersUsers()
        } else {
            viewModel.loadFollowedUsers()
        }
    }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = user?.username ?: "Người dùng",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.weight(1f))
            }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .background(Color.Black),
                        color = Color.Black
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(
                            text = tabTitle,
                            color = Color.Black
                        ) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val itemsToShow = if (selectedTabIndex == 0) followedUsers else followersUsers
                items(itemsToShow) { user ->
                    FollowItem(
                        user = user,
                        onUserClick = {
                            navController.navigate(
                                Screens.ProfileScreen.route + "/${user.userId}"
                            )
                        },
                        onFollowClick = { viewModel.unfollowUser(user.userId) },
                        isFollowing = selectedTabIndex == 0
                    )
                }
            }
        }
    }
@Composable
fun FollowItem(
    user: User,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit,
    isFollowing: Boolean
){
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
                    onClick = onUserClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profileImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(62.dp)
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
                    modifier = Modifier
                        .padding(bottom = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.blinkNotesId,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Button(
            onClick = onFollowClick,
            modifier = Modifier
                .height(36.dp)
                .width(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.beige)
            ),
            contentPadding = PaddingValues(horizontal = 0.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(
                text = if (isFollowing) "Đang Follow" else "Follow",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color =Color.Black,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold,
                    color =  Color.Black,
                ),
                maxLines = 1,
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}
