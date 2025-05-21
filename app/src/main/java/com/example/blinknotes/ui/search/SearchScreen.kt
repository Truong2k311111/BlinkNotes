import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.blinknotes.ui.home.ItemsFeed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.home.ExploreScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.colorResource
import android.content.Context
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController, context: Context) {
    val sharedPreferences = context.getSharedPreferences("blinknotes_prefs", Context.MODE_PRIVATE)
    val viewModel: SearchScreenViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SearchScreenViewModel(sharedPreferences) }
        }
    )
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState() // Observe search history
    val currentUser = FirebaseAuth.getInstance().currentUser

    val userId = currentUser?.uid ?: ""
    val viewModelEx  = ExploreScreenViewModel()
    val users by viewModelEx.users.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( vertical = 16.dp),
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
                        viewModel.searchPosts(searchText)
                        viewModel.addSearchHistory(searchText)
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
                                    viewModel.searchPosts(searchText)
                                    viewModel.addSearchHistory(searchText)
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

            LazyColumn (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchHistory) { keyword ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchText = keyword
                                viewModel.searchPosts(keyword) // Trigger search
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_timer),
                            contentDescription = "lock",
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .size(16.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = keyword,
                            modifier = Modifier
                                .weight(1f),
                            color = Color.Black,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(16.dp)
                                .clickable { viewModel.removeSearchHistory(keyword) },
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Hiển thị 2 cột
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
                        isFavorite = false, // Replace with actual favorite status if available
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
                        onLikeClick = { // Handle like click
                            viewModelEx.togglePostLike(post.id, userId)
                        }
                    )
                }
            }
        }

    }
}

