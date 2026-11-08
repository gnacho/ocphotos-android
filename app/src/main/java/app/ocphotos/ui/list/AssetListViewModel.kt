package app.ocphotos.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ocphotos.data.local.AssetEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Backs a list sourced from a Room Flow (favorites, archive).
class FlowAssetsViewModel(source: Flow<List<AssetEntity>>) : ViewModel() {
    val assets: StateFlow<List<AssetEntity>> = source
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

// Backs a list sourced from a one-shot suspend load (tag assets, album assets).
class LoadAssetsViewModel(
    private val load: suspend () -> List<AssetEntity>,
) : ViewModel() {
    private val _assets = MutableStateFlow<List<AssetEntity>>(emptyList())
    val assets: StateFlow<List<AssetEntity>> = _assets.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _assets.value = load()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = app.ocphotos.util.friendlyMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }
}
