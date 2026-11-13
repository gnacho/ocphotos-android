package app.ocphotos.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.toEntity
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.remote.dto.FolderDto
import app.ocphotos.data.repo.PhotosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import app.ocphotos.util.UiError

class FoldersViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _folders = MutableStateFlow<List<FolderDto>>(emptyList())
    val folders: StateFlow<List<FolderDto>> = _folders.asStateFlow()

    private val _assets = MutableStateFlow<List<AssetEntity>>(emptyList())
    val assets: StateFlow<List<AssetEntity>> = _assets.asStateFlow()

    // Breadcrumb stack of relative folder paths ("" = root).
    private val _pathStack = MutableStateFlow(listOf(""))
    val pathStack: StateFlow<List<String>> = _pathStack.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<UiError?>(null)
    val error: StateFlow<UiError?> = _error.asStateFlow()

    init {
        load("")
    }

    fun load(path: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resp = repository.folders(path.ifBlank { null })
                _folders.value = resp.folders
                _assets.value = resp.assets.map { it.toEntity() }
                _pathStack.value = breadcrumbs(resp.path)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = app.ocphotos.util.friendlyError(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun openFolder(name: String) {
        val current = _pathStack.value.lastOrNull() ?: ""
        load(if (current.isBlank()) name else "$current/$name")
    }

    fun up() {
        val stack = _pathStack.value
        if (stack.size <= 1) return
        val target = stack[stack.size - 2]
        load(target)
    }

    private fun breadcrumbs(path: String): List<String> {
        if (path.isBlank()) return listOf("")
        val parts = path.trim('/').split('/')
        val out = mutableListOf("")
        var acc = ""
        for (p in parts) {
            acc = if (acc.isBlank()) p else "$acc/$p"
            out += acc
        }
        return out
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { FoldersViewModel(repository) }
        }
    }
}
