package com.example.blinknotes.ui.notify

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.theme.ShimmerProfileItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifyScreen(
    navController: NavHostController,
    viewModel: NotifyViewModel = viewModel()
) {
    val loading = viewModel.loading.collectAsState().value
    var showSheet by remember { mutableStateOf(false) }
    var showSheetNotes by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val user = viewModel.users.collectAsState().value
    val userFriend = viewModel.usersFriend.collectAsState().value
    val currentUser = viewModel.currentUser.collectAsState().value
    val isOnline = viewModel.isActive.collectAsState().value
    val unreadMessagesCount = viewModel.unreadMessagesCount.collectAsState().value
    val unreadSystemNotifications by viewModel.unreadSystemNotifications.collectAsState()
    val unreadActivityNotifications by viewModel.unreadActivityNotifications.collectAsState()
    val latestSystemNotification by viewModel.latestSystemNotification.collectAsState()
    val latestActivityNotification by viewModel.latestActivityNotification.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUnreadMessagesCountForAllUsers()
    }
    Scaffold(
        topBar = {
            NotifyTopBar(
                isOnline = isOnline,
                onClickAddGroup = { /* TODO */ },
                onClickStatusBox = { showSheet = true },
                onClickSearch = { navController.navigate(Screens.SearchNotifyScreen.route) } // Navigate to SearchScreen
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (loading) {
                LazyColumn {
                    items(5) {
                        ShimmerProfileItem()
                    }
                }
            } else {
                NotifyContent(
                    modifier = Modifier.fillMaxWidth(),
                    notifyItems = userFriend,
                    navController = navController,
                    onNotifyItemClick = { userFriend ->
                        println("Clicked on ${userFriend.username}")
                    },
                    currentUserAvatar = currentUser?.profileImage ?: "",
                    onclick = { showSheetNotes = true },
                    viewModel = viewModel
                )
            }
        }
    }
    if( showSheetNotes) {
        ModalBottomSheet(
            onDismissRequest = { showSheetNotes = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(),
            containerColor = Color.White,
        ) {
           NotesScreen(
               onAddMomentClick = {},
               onPostClick = {},
               onCloseClick = { showSheetNotes = false },
               notes = "",
               onSettingsClick = {},
               currentUserAvatar = currentUser?.profileImage ?: "",
           )
        }
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.8f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White,
        ) {
            Box(Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .clickable {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) showSheet = false
                            }
                        }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    AsyncImage(
                        model = currentUser?.profileImage,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                if (isOnline) Color.Green else Color.Gray,
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Trạng thái hoạt động hiển thị với bạn bè",
                    fontSize = 20.sp,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = buildAnnotatedString {
                            append("• Cho bạn bè (follower mà bạn follow lại) biết bạn đang hoạt động. ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Người có thể nhìn thấy trạng thái hoạt động của bạn")
                            }
                        },
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Các bạn sẽ chỉ thấy trạng thái hoạt động của nhau nếu cả hai đều bật tính năng này",
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Sau khi bật, bạn sẽ được thông báo khi bạn bè của bạn trực tuyến và bạn sẽ thấy trạng thái của họ trong hộp thư của mình",
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(color = colorResource(R.color.gainsboro)) // màu xám
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trạng thái hoạt động",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked =  isOnline,
                        onCheckedChange = { viewModel.toggleActiveStatus() },

                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun NotifyContent(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    notifyItems: List<User>,
    onNotifyItemClick: (User) -> Unit,
    currentUserAvatar: String,
    onclick : () -> Unit = {},
    viewModel: NotifyViewModel,
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
        }
        item {
            TopRowComponent(
                currentUserAvatar = currentUserAvatar,
                friends = notifyItems.filter { it.userId != currentUserId },
                onAddMomentClick = {
                    navController.navigate(Screens.StatusScreen.route)
                },
                onAddFriendClick = {},
                onFriendClick = {},
                note = "Bạn có suy nghĩ?",
                onClick = onclick,

            )
        }
        item {
            FixedItemRow(
                title = "Hoạt động",
                subtitle = "Thông báo hoạt động mới nhất",
                onClick = { navController.navigate(Screens.ActivityNotificationScreen.route) },
                drawRes = R.drawable.bell,
                color = colorResource(R.color.greenyellow)
            )
        }
        item {
            FixedItemRow(
                title = "Thông báo hệ thống",
                subtitle = "Thông báo hệ thống mới nhất",
                onClick = { navController.navigate(Screens.SystemNotification.route) },
                drawRes = R.drawable.package_variant,
                color = colorResource(R.color.darkslategray)
            )
        }
        items(notifyItems.filter { it.userId != currentUserId }) { item -> // Filter out current user
            val encodedAvatar = URLEncoder.encode(item.profileImage, "UTF-8")
            LaunchedEffect(item) {
                notifyItems.forEach { item ->
                    viewModel.fetchLastMessageForUser(item.userId)

                }
            }
            NotifyItems(
                data = item,
                unreadCount = viewModel.unreadMessagesCount.value[item.userId] ?: 0,
                onClick = {
                    navController.navigate(
                        Screens.ChatScreen.route + "/${item.username}/${encodedAvatar}/${item.hasMoment}/${item.isMomentSeen}/${item.isOnline}/${item.userId}"
                    )
                    viewModel.markMessagesAsRead(item.userId)
                }
            )
        }
    }
}
@Composable
fun NotifyTopBar(
    isOnline: Boolean,
    onClickAddGroup: () -> Unit,
    onClickStatusBox: () -> Unit,
    onClickSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.account_multiple_plus),
            contentDescription = "Add Group",
            modifier = Modifier
                .size(28.dp)
                .clickable { onClickAddGroup() }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hộp Thư",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onClickStatusBox() }
                    .background(Color(0xFFD3D3D3))
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.circle),
                        contentDescription = "Status Circle",
                        modifier = Modifier.size(10.dp),
                        tint = if (isOnline) Color.Green else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.menu_down),
                        contentDescription = "Menu Down",
                        modifier = Modifier.size(12.dp),
                        tint = Color.DarkGray
                    )
                }
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.icon_search),
            contentDescription = "Search",
            modifier = Modifier
                .size(28.dp)
                .clickable { onClickSearch() }
        )
    }
}
@Composable
fun ItemsTopRow(
    avatar: String,
    username: String,
    note: String = "",
    hasStory: Boolean,
    isStorySeen: Boolean,
    onClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    isFriend: Boolean
) {

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clickable { onClick() }
        ) {
            if (hasStory) {
                val borderBrush = if (isStorySeen) {
                    SolidColor(colorResource( id = R.color.gainsboro))
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF00FF99),
                            Color(0xFF00C78A),
                            Color(0xFF00BEC2)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(5.dp, borderBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop

                    )
                }
            } else {
                AsyncImage(
                    model = avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop

                )
            }
            if (!isFriend) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(35.dp, 25.dp)
                        .clip(shape = RoundedCornerShape(50))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, shape = RoundedCornerShape(50))
                        .clickable { onAddFriendClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.account_plus),
                        contentDescription = "Add Friend",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    )
                }
            }
            if (note.isNotEmpty()) {
                BubbleNote(text = note,onClick={})
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = username,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
fun TopRowComponent(
    currentUserAvatar: String,
    friends: List<User>,
    onAddMomentClick: () -> Unit,
    onFriendClick: (User) -> Unit,
    onAddFriendClick: (User) -> Unit,
    note: String = "",
    viewModel: NotifyViewModel = viewModel(),
    onClick : () -> Unit = {},
) {
    val friendStatusMap by viewModel.friendStatusMap.collectAsState()

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onAddMomentClick() },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentUserAvatar,
                        contentDescription = "Your Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 1.dp, y = 4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00CCFF))
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    BubbleNote(text = "Bạn có suy nghĩ?",onClick = onClick)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Đăng",
                    fontSize = 12.sp
                )
            }
        }
        items(friends) { friend ->
            LaunchedEffect (Unit) {
                viewModel.checkIfFriend(friend.userId)
            }
            val isFriend = friendStatusMap[friend.userId] == true
            ItemsTopRow(
                avatar = friend.profileImage,
                username = friend.username,
                hasStory = friend.hasStory,
                isStorySeen = friend.isStorySeen,
                onClick = {
                    if (friend.hasStory)
                        onFriendClick(friend)
                    else
                        onFriendClick(friend)
                },
                onAddFriendClick = {
                    onAddFriendClick(friend)
                },
                note = friend.note,
                isFriend = isFriend,
            )
        }
    }
}
@Composable
fun BubbleNote(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(90.dp, 70.dp)

    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .offset( x = (12).dp,y = (-30).dp)
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onClick()
                        },
                    )
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF000000),
                        spotColor = Color(0xFF000000)
                    )
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = text,
                    fontSize = 11.sp,
                    color = Color.Black,
                    maxLines = 2,
                    lineHeight = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(8.dp)
                        .width(80.dp)
                )
            }
        }
    }
}


@Composable
fun FixedItemRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    drawRes : Int,
    color: Color

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color = color, shape = CircleShape)
                .clickable {

                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = drawRes),
                contentDescription = "Your Icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.icon_arrow_right),
            contentDescription = "Next",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun NotifyItems(
    data: User,
    onClick: () -> Unit,
    unreadCount: Int,
    viewModel: NotifyViewModel = viewModel()
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (data.hasMoment) {
                val borderBrush = if (data.isMomentSeen) {
                    SolidColor(colorResource(id = R.color.gainsboro))
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF00FF99),
                            Color(0xFF00C78A),
                            Color(0xFF00BEC2)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(3.dp, borderBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = data.profileImage,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop

                    )
                }
            } else {
                AsyncImage(
                    model = data.profileImage,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop

                )
            }
            if (data.isOnline) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Green, CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = data.username,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text =
                    viewModel.lastMessages.collectAsState().value[data.userId] ?: "",
                fontSize = 14.sp,
                color = if (unreadCount > 0) Color.Black else Color.Gray,
                maxLines = 1,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 5) "5+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Camera",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
