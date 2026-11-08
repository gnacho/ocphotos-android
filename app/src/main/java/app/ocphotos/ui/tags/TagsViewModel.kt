package app.ocphotos.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.remote.dto.TagDto
import app.ocphotos.data.repo.PhotosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TagsViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _tags = MutableStateFlow<List<TagDto>>(emptyList())
    val tags: StateFlow<List<TagDto>> = _tags.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _tags.value = repository.tags()
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
            initializer { TagsViewModel(repository) }
        }
    }
}
