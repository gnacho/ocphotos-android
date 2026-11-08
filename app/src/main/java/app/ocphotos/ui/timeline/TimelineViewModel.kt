package app.ocphotos.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimelineViewModel(private val repository: PhotosRepository) : ViewModel() {

    val assets: StateFlow<List<AssetEntity>> = repository.timeline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    init {
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _syncing.value = true
            _syncError.value = null
            try {
                repository.sync()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _syncError.value = app.ocphotos.util.friendlyMessage(e)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun toggleFavorite(asset: AssetEntity) {
        viewModelScope.launch { runCatching { repository.setFavorite(asset.id, !asset.favorite) } }
    }

    fun rescan() {
        viewModelScope.launch {
            runCatching { repository.rescan() }
            sync()
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { TimelineViewModel(repository) }
        }
    }
}
