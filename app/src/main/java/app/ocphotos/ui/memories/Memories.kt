package app.ocphotos.ui.memories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.data.remote.dto.MemoriesResponse
import app.ocphotos.data.remote.dto.YearDto
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.LocalRepository
import app.ocphotos.ui.components.StatusBox
import coil3.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemoriesViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _hero = MutableStateFlow<MemoriesResponse?>(null)
    val hero: StateFlow<MemoriesResponse?> = _hero.asStateFlow()

    private val _onThisDay = MutableStateFlow<List<AssetEntity>>(emptyList())
    val onThisDay: StateFlow<List<AssetEntity>> = _onThisDay.asStateFlow()

    private val _years = MutableStateFlow<List<YearDto>>(emptyList())
    val years: StateFlow<List<YearDto>> = _years.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _hero.value = repository.highlights()
                _onThisDay.value = repository.onThisDay()
                _years.value = repository.calendar().years
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Load failed"
            } finally {
                _loading.value = false
            }
        }
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { MemoriesViewModel(repository) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoriesScreen(
    viewModel: MemoriesViewModel,
    onOpen: (Long) -> Unit,
) {
    val hero by viewModel.hero.collectAsState()
    val onThisDay by viewModel.onThisDay.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val repo = LocalRepository.current

    if (loading && onThisDay.isEmpty()) {
        StatusBox(loading = true, error = null, empty = false, emptyLabel = "")
        return
    }
    if (onThisDay.isEmpty()) {
        StatusBox(loading = false, error = error, empty = error == null, emptyLabel = "No memories today")
        return
    }

    LazyColumn(Modifier.fillMaxSize()) {
        hero?.assets?.firstOrNull()?.let { first ->
            item(key = "hero") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable { onOpen(first.id) },
                ) {
                    AsyncImage(
                        model = repo.thumbUrl(first.id, 2048),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = "On this day \u00B7 ${hero?.scope ?: ""}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                    )
                }
            }
        }

        item(key = "header") {
            Text(
                "On this day",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp),
            )
        }

        item(key = "grid") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            ) {
                onThisDay.forEach { asset ->
                    AssetThumb(
                        asset = asset,
                        modifier = Modifier
                            .size(110.dp)
                            .clickable { onOpen(asset.id) },
                    )
                }
            }
        }
    }
}
