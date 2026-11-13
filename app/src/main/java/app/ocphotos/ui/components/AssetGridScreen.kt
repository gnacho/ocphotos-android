package app.ocphotos.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.util.UiError

// Generic asset grid with loading/error/empty handling, used by Favorites, Archive,
// tag results and album contents.
@Composable
fun AssetGridScreen(
    assets: List<AssetEntity>,
    loading: Boolean,
    error: UiError?,
    @StringRes emptyLabel: Int,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        loading && assets.isEmpty() ->
            StatusBox(loading = true, error = null, empty = false, modifier = modifier)
        assets.isEmpty() && error != null ->
            StatusBox(loading = false, error = error, empty = false, modifier = modifier)
        assets.isEmpty() ->
            StatusBox(loading = false, error = null, empty = true, emptyLabel = emptyLabel, modifier = modifier)
        else -> PhotoGrid(assets = assets, onOpen = onOpen, modifier = modifier)
    }
}
