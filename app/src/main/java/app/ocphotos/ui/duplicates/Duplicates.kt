package app.ocphotos.ui.duplicates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.toEntity
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.StatusBox
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import app.ocphotos.util.UiError
import androidx.compose.ui.res.pluralStringResource
import app.ocphotos.R

class DuplicatesViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _groups = MutableStateFlow<List<List<AssetEntity>>>(emptyList())
    val groups: StateFlow<List<List<AssetEntity>>> = _groups.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<UiError?>(null)
    val error: StateFlow<UiError?> = _error.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _groups.value = repository.duplicates().groups
                    .filter { it.size > 1 }
                    .map { g -> g.map { it.toEntity() } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = app.ocphotos.util.friendlyError(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun archive(asset: AssetEntity) {
        viewModelScope.launch {
            runCatching { repository.setArchived(asset.id, true) }
            load()
        }
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { DuplicatesViewModel(repository) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuplicatesScreen(
    viewModel: DuplicatesViewModel,
    onOpen: (Long) -> Unit,
) {
    val groups by viewModel.groups.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (loading && groups.isEmpty()) {
        StatusBox(loading = true, error = null, empty = false)
        return
    }
    if (groups.isEmpty()) {
        StatusBox(loading = false, error = error, empty = error == null, emptyLabel = R.string.empty_duplicates)
        return
    }

    LazyColumn(Modifier.fillMaxSize()) {
        groups.forEachIndexed { index, group ->
            item(key = "group-$index") {
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(
                        pluralStringResource(
                            R.plurals.duplicate_group,
                            group.size,
                            index + 1,
                            group.size,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    TextButton(
                        onClick = { group.drop(1).forEach { viewModel.archive(it) } },
                    ) {
                        Text(
                            pluralStringResource(
                                R.plurals.duplicates_archive,
                                group.size - 1,
                                group.size - 1,
                            ),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        group.forEach { asset ->
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
    }
}
