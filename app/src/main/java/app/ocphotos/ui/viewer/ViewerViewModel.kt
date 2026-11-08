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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewerViewModel(
    private val repository: PhotosRepository,
    private val assetId: Long,
) : ViewModel() {

    private val _asset = MutableStateFlow<AssetEntity?>(null)
    val asset: StateFlow<AssetEntity?> = _asset.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _albums = MutableStateFlow<List<AlbumEntity>>(emptyList())
    val albums: StateFlow<List<AlbumEntity>> = _albums.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            _asset.value = repository.assetById(assetId)
            runCatching { _tags.value = repository.assetTags(assetId) }
            runCatching { _albums.value = repository.refreshAlbums() }
        }
    }

    fun toggleFavorite() {
        val a = _asset.value ?: return
        viewModelScope.launch {
            runCatching { repository.setFavorite(a.id, !a.favorite) }
            _asset.value = a.copy(favorite = !a.favorite)
        }
    }

    fun toggleArchived() {
        val a = _asset.value ?: return
        viewModelScope.launch {
            runCatching { repository.setArchived(a.id, !a.archived) }
            _asset.value = a.copy(archived = !a.archived)
        }
    }

    fun addTag(tag: String) {
        val a = _asset.value ?: return
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching { repository.addTag(a.id, trimmed) }
            if (trimmed !in _tags.value) _tags.value = _tags.value + trimmed
        }
    }

    fun removeTag(tag: String) {
        val a = _asset.value ?: return
        viewModelScope.launch {
            runCatching { repository.removeTag(a.id, tag) }
            _tags.value = _tags.value - tag
        }
    }

    fun addToAlbum(albumId: Long) {
        val a = _asset.value ?: return
        viewModelScope.launch { runCatching { repository.addToAlbum(albumId, listOf(a.id)) } }
    }

    suspend fun videoUrl(): String? {
        val a = _asset.value ?: return null
        return try {
            repository.videoUrl(a.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = app.ocphotos.util.friendlyMessage(e)
            null
        }
    }

    companion object {
        fun factory(repository: PhotosRepository, assetId: Long) = viewModelFactory {
            initializer { ViewerViewModel(repository, assetId) }
        }
    }
}
