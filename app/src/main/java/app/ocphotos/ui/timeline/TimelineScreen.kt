package app.ocphotos.ui.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.StatusBox
import app.ocphotos.util.TimeFormat
import app.ocphotos.util.groupByDay

@Composable
fun TimelineScreen(
    assets: List<AssetEntity>,
    syncing: Boolean,
    syncError: String?,
    onOpen: (Long) -> Unit,
    onToggleFavorite: (AssetEntity) -> Unit,
    onSync: () -> Unit,
) {
    val grouped = remember(assets) { groupByDay(assets) }

    if (assets.isEmpty() && syncing) {
        StatusBox(loading = true, error = null, empty = false, emptyLabel = "")
        return
    }
    if (assets.isEmpty() && syncError != null) {
        StatusBox(loading = false, error = syncError, empty = false, emptyLabel = "")
        return
    }
    if (assets.isEmpty()) {
        StatusBox(loading = false, error = null, empty = true, emptyLabel = "No photos yet")
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        grouped.forEach { (_, list) ->
            val first = list.first()
            item(span = { GridItemSpan(maxLineSpan) }, key = "day-${first.takenAt}") {
                Text(
                    text = TimeFormat.formatFullDate(first.takenAt),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            items(list, key = { it.id }) { asset ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onOpen(asset.id) },
                ) {
                    AssetThumb(asset = asset, modifier = Modifier.fillMaxSize())
                    if (asset.favorite) {
                        Text(
                            text = "\u2665",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                        )
                    }
                    if (asset.mediaType == "video") {
                        Text(
                            text = "\u25B6",
                            color = androidx.compose.ui.graphics.Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                        )
                    }
                }
            }
        }
    }
}
