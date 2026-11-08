package app.ocphotos.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.ui.components.AssetGridScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _results = MutableStateFlow<List<AssetEntity>>(emptyList())
    val results: StateFlow<List<AssetEntity>> = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searched = MutableStateFlow(false)
    val searched: StateFlow<Boolean> = _searched.asStateFlow()

    fun search(q: String) {
        val query = q.trim()
        if (query.isEmpty()) {
            _results.value = emptyList()
            _searched.value = false
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _searched.value = true
            _error.value = null
            try {
                _results.value = repository.searchRemote(query)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = app.ocphotos.util.friendlyMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { ExploreViewModel(repository) }
        }
    }
}

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onOpen: (Long) -> Unit,
) {
    val results by viewModel.results.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searched by viewModel.searched.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search photos") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { viewModel.search(query) }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        AssetGridScreen(
            assets = results,
            loading = loading,
            error = error,
            emptyLabel = if (searched) "No results" else "Search your photos",
            onOpen = onOpen,
        )
    }
}
