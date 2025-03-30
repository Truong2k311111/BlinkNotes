package com.example.blinknotes.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



data class HomeScreenViewModelData(
    val listFeed : List<Feed> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageLink: String? = null,
    val avatarLink: String? = null,
    val status: String? = null,
    val numBerHeart: Int? = 0,
    val userName: String? = null,
)
data class Feed(
    val imageLink: String? = null,
    val avatarLink: String? = null,
    val status: String? = null,
    val numBerHeart: Int? = 0,
    val userName: String? = null,
)
// class HomeScreenViewModelFactory : ViewModelProvider.Factory {
//         override fun <T : ViewModel> create(modelClass: Class<T>): T {
//             return if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
//                 HomeScreenViewModel() as T
//             } else {
//                 throw IllegalArgumentException("ViewModel Not Found")
//             }
//         }
// }
class HomeScreenViewModel : ViewModel() {
    private val viewModelState = MutableStateFlow(HomeScreenViewModelData())
    val uiState: StateFlow<HomeScreenViewModelData> = viewModelState


//    private val exploreScreenViewModel = ExploreScreenViewModel()
//
//    val posts = exploreScreenViewModel.posts
//
//    init {
//        exploreScreenViewModel.getAllPosts() // Lấy danh sách bài viết ngay khi ViewModel được tạo
//    }

//        .onStart {
//            loadImages()
//            Log.e("aaa","onStart")
//        }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.Eagerly,
//            viewModelState.value
//        )
    companion object {
        private const val PAGE_SIZE = 10
        private const val LOAD_DELAY_MS = 2000L
    }
    private var loadedCount = 0

}