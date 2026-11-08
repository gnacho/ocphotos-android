package app.ocphotos.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.local.AlbumEntity
import app.ocphotos.data.repo.PhotosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlbumsViewModel(private val repository: PhotosRepository) : ViewModel() {

    val albums: StateFlow<List<AlbumEntity>> = repository.albumsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { runCatching { repository.refreshAlbums() } }
    }

    fun create(name: String) {
        viewModelScope.launch {
            runCatching { repository.createAlbum(name) }
            refresh()
        }
    }

    fun rename(id: Long, name: String) {
        viewModelScope.launch {
            runCatching { repository.renameAlbum(id, name) }
            refresh()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteAlbum(id) }
            refresh()
        }
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { AlbumsViewModel(repository) }
        }
    }
}
