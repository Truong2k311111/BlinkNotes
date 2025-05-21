import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.ui.home.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchScreenViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _searchResults = MutableStateFlow<List<Post>>(emptyList())
    val searchResults: StateFlow<List<Post>> = _searchResults

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    init {
        loadSearchHistory()
    }

    fun searchPosts(query: String) {
        viewModelScope.launch {
            try {
                db.collection("posts")
                    .get()
                    .addOnSuccessListener { documents ->
                        val results = documents.mapNotNull { doc ->
                            val post = doc.toObject(Post::class.java).copy(id = doc.id)
                            if (post.caption.contains(query, ignoreCase = true) ||
                                post.tags.any { it.contains(query, ignoreCase = true) }
                            ) {
                                post
                            } else null
                        }
                        _searchResults.value = results
                        Log.d("SearchScreenViewModel", "Search results: $results")
                    }
            } catch (e: Exception) {
                Log.e("SearchScreenViewModel", "Error searching posts: ${e.message}")
            }
        }
    }

    private fun loadSearchHistory() {
        val savedHistory = sharedPreferences.getStringSet("search_history", emptySet()) ?: emptySet()
        _searchHistory.value = savedHistory.toList()
    }

    private fun saveSearchHistory(history: List<String>) {
        sharedPreferences.edit()
            .putStringSet("search_history", history.toSet())
            .apply()
    }

    fun addSearchHistory(keyword: String) {
        _searchHistory.update { history ->
            val updatedHistory = if (!history.contains(keyword)) {
                (history + keyword).takeLast(10) // Keep only the last 10 items
            } else history
            saveSearchHistory(updatedHistory)
            updatedHistory
        }
    }

    fun removeSearchHistory(keyword: String) {
        _searchHistory.update { history ->
            val updatedHistory = history - keyword
            saveSearchHistory(updatedHistory)
            updatedHistory
        }
    }
}
