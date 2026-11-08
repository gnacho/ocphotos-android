package app.ocphotos.ui.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.StatusBox
import app.ocphotos.util.TimeFormat
import app.ocphotos.util.groupByDay
import kotlinx.coroutines.launch

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

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Map year -> grid item index of the first day header of that year. Item indices
    // count the day headers too (one header per day plus its assets).
    val yearItemIndex = remember(assets) {
        val map = LinkedHashMap<Int, Int>()
        var itemIndex = 0
        for ((_, list) in grouped) {
            val year = TimeFormat.year(list.first().takenAt)
            if (year !in map) map[year] = itemIndex
            itemIndex += 1 + list.size
        }
        map
    }

    Row(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            state = gridState,
            modifier = Modifier.weight(1f).fillMaxSize(),
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

        // Rewind scrubber: one entry per year, jump to the first day of that year.
        Column(
            modifier = Modifier
                .width(52.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            yearItemIndex.keys.forEach { year ->
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { gridState.scrollToItem(yearItemIndex[year] ?: 0) }
                        }
                        .padding(vertical = 10.dp),
                )
            }
        }
    }
}
