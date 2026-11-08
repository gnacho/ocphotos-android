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

    val asset: StateFlow<AssetEntity?> = MutableStateFlow(null)
    val tags: StateFlow<List<String>> = MutableStateFlow(emptyList())
    val albums: StateFlow<List<AlbumEntity>> = MutableStateFlow(emptyList())
    val error: StateFlow<String?> = MutableStateFlow(null)

    private val _asset = asset as MutableStateFlow
    private val _tags = tags as MutableStateFlow
    private val _albums = albums as MutableStateFlow
    private val _error = error as MutableStateFlow

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
            _error.value = e.message
            null
        }
    }

    companion object {
        fun factory(repository: PhotosRepository, assetId: Long) = viewModelFactory {
            initializer { ViewerViewModel(repository, assetId) }
        }
    }
}
