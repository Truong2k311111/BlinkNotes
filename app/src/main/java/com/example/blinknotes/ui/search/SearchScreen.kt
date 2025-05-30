import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.home.ExploreScreenViewModel
import com.example.blinknotes.ui.home.ItemsFeed
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    context: Context,
    initialQuery: String = ""
) {
    val sharedPreferences = context.getSharedPreferences("blinknotes_prefs", Context.MODE_PRIVATE)
    val viewModel: SearchScreenViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SearchScreenViewModel(sharedPreferences) }
        }
    )
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val userId = currentUser?.uid ?: ""
    val viewModelEx  = ExploreScreenViewModel()
    val users by viewModelEx.users.collectAsState()
    var isHistoryExpanded by remember { mutableStateOf(false) }
    fun doSearch(text: String) {
        if (text.isNotEmpty()) {
            viewModel.searchPosts(text)
            viewModel.addSearchHistory(text)
            isHistoryExpanded = false
        }
    }
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && initialQuery != "{query}") {
            searchText = initialQuery
            viewModel.searchPosts(initialQuery)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = colorResource(R.color.beige), shape = RoundedCornerShape(32.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (searchText.isNotEmpty()) {
                            doSearch(searchText)
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập từ khóa tìm kiếm",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                        ) },
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                Text(
                    text = "Tìm kiếm",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (searchText.isNotEmpty()) {
                                    doSearch(searchText)
                                }
                            }
                        ),
                    color = Color.Red
                )
            }
        },
        contentColor = Color.White,
        containerColor = Color.White
    ) {
        innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Spacer(modifier = Modifier.height(8.dp))

            SearchHistorySection(
                searchHistory = searchHistory,
                onSearch = { keyword ->
                    searchText = keyword
                    doSearch(keyword)
                },
                onRemoveHistory = { keyword ->
                    viewModel.removeSearchHistory(keyword)
                },
                isExpanded = isHistoryExpanded,
                onExpandToggle = { isHistoryExpanded = !isHistoryExpanded }
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { post ->
                    LaunchedEffect(post.userId) {
                        if (!users.containsKey(post.userId)) {
                            viewModelEx.fetchUser(post.userId)
                        }
                        viewModelEx.checkPostLikeStatus(post.id, userId)
                    }
                    val user = users[post.userId]
                    val isFavorite by remember(post.id) {
                        derivedStateOf {
                            viewModelEx.postLikeStatus.value[post.id] ?: false
                        }
                    }
                    Log.d("SearchScreen", "User: ${post.firstImageUrl}")
                    ItemsFeed(
                        imageLink = post.imageUrls.firstOrNull() ?: "",
                        numberHeart = post.likesCount,
                        status = post.caption,
                        userName = user?.username ?: "",
                        profileImage = user?.profileImage ?: "",
                        isFavorite = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(12.dp)),
                        onclick = {
                            viewModelEx.getPostByPostId(post.id) { post ->
                                post?.let {
                                    navController.navigate(Screens.DetaillScreen.route + "/${it.id}/${userId}")
                                }
                            }
                        },
                        onLikeClick = {
                            viewModelEx.togglePostLike(post.id, userId)
                        }
                    )
                }
            }
        }

    }
}
@Composable
fun SearchHistorySection(
    searchHistory: List<String>,
    onSearch: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = if (isExpanded) Dp.Infinity else screenHeight / 2

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    max = if (isExpanded) maxHeight else 48.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isExpanded) Modifier else Modifier
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val itemsToShow = if (isExpanded) searchHistory else searchHistory.take(1)
                items(itemsToShow) { keyword ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearch(keyword) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_timer),
                            contentDescription = "Lịch sử",
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .size(16.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = keyword,
                            modifier = Modifier.weight(1f),
                            color = Color.Black,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(16.dp)
                                .clickable { onRemoveHistory(keyword) },
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
        if (searchHistory.isNotEmpty()) {
            TextButton(
                onClick = onExpandToggle,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Text(text = if (isExpanded) "Thu gọn" else "Xem thêm"
                , color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
