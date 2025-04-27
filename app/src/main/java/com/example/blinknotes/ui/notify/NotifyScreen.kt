package com.example.blinknotes.ui.notify

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blinknotes.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.navigation.Screens
import java.net.URLEncoder
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.blinknotes.ui.home.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifyScreen(
    navController: NavHostController,
    viewModel: NotifyViewModel = viewModel()
) {
    val notifyItems = viewModel.notifyItems.collectAsState().value
    val loading = viewModel.loading.collectAsState().value
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val user = viewModel.users.collectAsState().value
    val currentUser = viewModel.currentUser.collectAsState().value
    val isActive = currentUser?.isOnline ?: false


    Scaffold(
        topBar = {
            NotifyTopBar(
                isActive = isActive,
                isOnline = currentUser?.isOnline ?: false, // Pass isOnline status
                onClickAddGroup = { /* TODO */ },
                onClickStatusBox = { showSheet = true },
                onClickSearch = { navController.navigate(Screens.SearchNotifyScreen.route) } // Navigate to SearchScreen
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                NotifyContent(
                    modifier = Modifier.fillMaxWidth(),
                    notifyItems = user,
                    navController = navController,
                    onNotifyItemClick = { user ->
                        println("Clicked on ${user.username}")
                    }
                )
            }
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

                // Avatar + trạng thái
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.accounticon),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                if (isActive) Color.Green else Color.Gray,
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

                // Nội dung mô tả
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
                // Hàng chuyển đổi trạng thái
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
                        checked = isActive,
                        onCheckedChange = { viewModel.toggleActiveStatus() }
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
    onNotifyItemClick: (User) -> Unit
) {

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
                currentUserAvatar = painterResource(id = R.drawable.accounticon),
                friends = notifyItems,
                onAddMomentClick = { println("Add moment") },
                onAddFriendClick = {},
                onFriendClick = {},
                note = "Bạn có suy nghĩ?",

            )
        }

        item {
            FixedItemRow(
                title = "Những Follower mới",
                subtitle = "Thông báo mới nhất",
                onClick = {  },
                drawRes = R.drawable.account_multiple_plus,
                color = colorResource(R.color.deeppink)
            )
        }
        item {
            FixedItemRow(
                title = "Hoạt động",
                subtitle = "Thông báo hoạt động mới nhất",
                onClick = {  },
                drawRes = R.drawable.bell,
                color = colorResource(R.color.greenyellow)
            )
        }
        item {
            FixedItemRow(
                title = "Thông báo hệ thống",
                subtitle = "Thông báo hệ thống mới nhất",
                onClick = {  },
                drawRes = R.drawable.package_variant,
                color = colorResource(R.color.darkslategray)
            )
        }

        // Dynamic NotifyItems
        items(notifyItems) { item ->
            val encodedAvatar = URLEncoder.encode(item.profileImage, "UTF-8")
            NotifyItems(
                data = item,
                onClick = {
                    navController.navigate(
                        Screens.ChatScreen.route + "/${item.username}/${encodedAvatar}/${item.hasMoment}/${item.isMomentSeen}/${item.isOnline}"
                    )
                }
            )
        }
    }
}
@Composable
fun NotifyTopBar(
    isActive: Boolean,
    isOnline: Boolean, // Add isOnline parameter
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
                        tint = if (isOnline) Color.Green else Color.Gray // Use isOnline status
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
    isFriend: Boolean,
    onClick: () -> Unit,
    onAddFriendClick: () -> Unit
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

            // Viền nếu có story
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

            // Nếu chưa kết bạn => hiện Box icon
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
                BubbleNote(text = note)
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
    currentUserAvatar: Painter,
    friends: List<User>,
    onAddMomentClick: () -> Unit,
    onFriendClick: (User) -> Unit,
    onAddFriendClick: (User) -> Unit,
    note: String = ""
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Người dùng hiện tại
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clickable { onAddMomentClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = currentUserAvatar,
                        contentDescription = "Your Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
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
                    BubbleNote(text = "Bạn có suy nghĩ?")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Đăng",
                    fontSize = 12.sp
                )
            }
        }

        // Danh sách bạn bè
        items(friends) { friend ->
            ItemsTopRow(
                avatar = friend.profileImage,
                username = friend.username,
                hasStory = friend.hasStory,
                isStorySeen = friend.isStorySeen,
                isFriend = friend.isFriend,
                onClick = {
                    if (friend.hasStory)
                        onFriendClick(friend) // sang màn hình story
                    else
                        onFriendClick(friend) // sang màn hình chat
                },
                onAddFriendClick = {
                    onAddFriendClick(friend)
                },
                note = friend.note
            )
        }
    }
}
@Composable
fun BubbleNote(
    text: String,
    modifier: Modifier = Modifier
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
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF000000), // màu bóng nhẹ
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

//            Canvas(
//                modifier = Modifier
//                    .size(10.dp)
//                    .offset(x = 16.dp) // lệch sang trái 1 chút để giống TikTok
//            ) {
//                drawPath(
//                    path = Path().apply {
//                        moveTo(0f, 0f)
//                        lineTo(size.width / 2, size.height)
//                        lineTo(size.width, 0f)
//                        close()
//                    },
//                    color = Color.White
//                )
//            }
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
                .size(40.dp) // Kích thước vòng tròn
                .background(color = color, shape = CircleShape) // màu đen xanh, bạn tùy chỉnh thêm
                .clickable { /* Handle click */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = drawRes),
                contentDescription = "Your Icon",
                tint = Color.White, // icon màu trắng
                modifier = Modifier.size(20.dp) // Kích thước icon bên trong
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp),
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
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop

                    )
                }
            } else {
                AsyncImage(
                    model = data.profileImage,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop

                )
            }

            if (data.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Green, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
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
                text = data.latestMessage,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        if (data.unreadMessages > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (data.unreadMessages > 5) "5+" else data.unreadMessages.toString(),
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

