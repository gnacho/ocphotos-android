package app.ocphotos.ui.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.local.AlbumEntity
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import app.ocphotos.util.UiError

class ViewerViewModel(
    private val repository: PhotosRepository,
    val assetId: Long,
) : ViewModel() {

    // The swipe surface: every timeline asset, in the same order as the timeline.
    // The initial page is derived from assetId in the screen.
    val assets: StateFlow<List<AssetEntity>> = repository.timeline
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _albums = MutableStateFlow<List<AlbumEntity>>(emptyList())
    val albums: StateFlow<List<AlbumEntity>> = _albums.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _error = MutableStateFlow<UiError?>(null)
    val error: StateFlow<UiError?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { _albums.value = repository.refreshAlbums() }
        }
    }

    fun loadTags(asset: AssetEntity) {
        viewModelScope.launch {
            runCatching { _tags.value = repository.assetTags(asset.id) }
        }
    }

    fun toggleFavorite(asset: AssetEntity) {
        viewModelScope.launch { runCatching { repository.setFavorite(asset.id, !asset.favorite) } }
    }

    fun toggleArchived(asset: AssetEntity) {
        viewModelScope.launch { runCatching { repository.setArchived(asset.id, !asset.archived) } }
    }

    fun addTag(asset: AssetEntity, tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching { repository.addTag(asset.id, trimmed) }
            if (trimmed !in _tags.value) _tags.value = _tags.value + trimmed
        }
    }

    fun removeTag(asset: AssetEntity, tag: String) {
        viewModelScope.launch {
            runCatching { repository.removeTag(asset.id, tag) }
            _tags.value = _tags.value - tag
        }
    }

    fun addToAlbum(asset: AssetEntity, albumId: Long) {
        viewModelScope.launch { runCatching { repository.addToAlbum(albumId, listOf(asset.id)) } }
    }

    suspend fun videoUrl(asset: AssetEntity): String? = try {
        repository.videoUrl(asset.id)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        _error.value = app.ocphotos.util.friendlyError(e)
        null
    }

    companion object {
        fun factory(repository: PhotosRepository, assetId: Long) = viewModelFactory {
            initializer { ViewerViewModel(repository, assetId) }
        }
    }
}
