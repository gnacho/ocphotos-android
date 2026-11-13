package app.ocphotos.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.data.remote.dto.PlaceDto
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.StatusBox
import app.ocphotos.util.Geo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import app.ocphotos.util.UiError
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import app.ocphotos.R

class MapViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _places = MutableStateFlow<List<PlaceDto>>(emptyList())
    val places: StateFlow<List<PlaceDto>> = _places.asStateFlow()

    private val _geo = MutableStateFlow<List<AssetEntity>>(emptyList())
    val geo: StateFlow<List<AssetEntity>> = _geo.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<UiError?>(null)
    val error: StateFlow<UiError?> = _error.asStateFlow()

    private val _selected = MutableStateFlow<PlaceDto?>(null)
    val selected: StateFlow<PlaceDto?> = _selected.asStateFlow()

    private val _selectedAssets = MutableStateFlow<List<AssetEntity>>(emptyList())
    val selectedAssets: StateFlow<List<AssetEntity>> = _selectedAssets.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _places.value = repository.places()
                _geo.value = repository.geo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = app.ocphotos.util.friendlyError(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun select(place: PlaceDto) {
        _selected.value = place
        _selectedAssets.value = _geo.value.filter { a ->
            a.lat != null && a.lon != null &&
                Geo.round(a.lat) == Geo.round(place.lat) &&
                Geo.round(a.lon) == Geo.round(place.lon)
        }
    }

    fun clearSelection() {
        _selected.value = null
        _selectedAssets.value = emptyList()
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { MapViewModel(repository) }
        }
    }
}

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onOpenAsset: (Long) -> Unit,
) {
    val places by viewModel.places.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val selectedAssets by viewModel.selectedAssets.collectAsState()
    val context = LocalContext.current
    val resources = context.resources

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    if (loading && places.isEmpty()) {
        StatusBox(loading = true, error = null, empty = false)
        return
    }
    if (places.isEmpty()) {
        StatusBox(loading = false, error = error, empty = error == null, emptyLabel = R.string.empty_geo)
        return
    }

    Column(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            update = { mv ->
                mv.overlays.clear()
                places.forEach { place ->
                    val marker = Marker(mv).apply {
                        position = GeoPoint(place.lat, place.lon)
                        title = place.name ?: "%.3f, %.3f".format(place.lat, place.lon)
                        snippet = resources.getQuantityString(R.plurals.place_photos, place.count, place.count)
                        setOnMarkerClickListener { _, _ ->
                            viewModel.select(place)
                            true
                        }
                    }
                    mv.overlays.add(marker)
                }
                if (places.isNotEmpty()) {
                    mv.controller.setZoom(5.0)
                }
                mv.invalidate()
            },
        )

        if (selected != null) {
            Surface(tonalElevation = 4.dp) {
                Column(Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = selected?.name ?: stringResource(R.string.nav_photos),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.action_close),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.clearSelection() }.padding(4.dp),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        selectedAssets.forEach { asset ->
                            AssetThumb(
                                asset = asset,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clickable { onOpenAsset(asset.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
