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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.StatusBox
import app.ocphotos.util.TimeFormat
import app.ocphotos.util.groupByDay
import kotlinx.coroutines.launch

private data class MonthRef(val month: Int, val itemIndex: Int)
private data class YearRef(val year: Int, val itemIndex: Int, val months: MutableList<MonthRef>)

@Composable
fun TimelineScreen(
    assets: List<AssetEntity>,
    syncing: Boolean,
    syncError: String?,
    onOpen: (Long) -> Unit,
    onToggleFavorite: (AssetEntity) -> Unit,
    onSync: () -> Unit,
    onRescan: () -> Unit,
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
        StatusBox(
            loading = false,
            error = null,
            empty = true,
            emptyLabel = "No photos yet",
            actionLabel = "Rescan",
            onAction = onRescan,
        )
        return
    }

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Year -> month tree with the grid item index of the first day header of each.
    val scrubberYears = remember(assets) {
        val years = LinkedHashMap<Int, YearRef>()
        val monthSeen = mutableSetOf<String>()
        var itemIndex = 0
        for ((_, list) in grouped) {
            val first = list.first()
            val year = TimeFormat.year(first.takenAt)
            val month = TimeFormat.month(first.takenAt)
            val ref = years.getOrPut(year) { YearRef(year, itemIndex, mutableListOf()) }
            val key = "$year-$month"
            if (key !in monthSeen) {
                monthSeen += key
                ref.months += MonthRef(month, itemIndex)
            }
            itemIndex += 1 + list.size
        }
        years.values.toList()
    }

    var expandedYear by remember { mutableStateOf<Int?>(null) }

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
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                            )
                        }
                        if (asset.mediaType == "video") {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                            )
                        }
                    }
                }
            }
        }

        // Rewind scrubber: years expand to months; both jump the grid to the first
        // day header of that year/month.
        Column(
            modifier = Modifier
                .width(56.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            scrubberYears.forEach { yearRef ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            scope.launch { gridState.scrollToItem(yearRef.itemIndex) }
                            expandedYear = if (expandedYear == yearRef.year) null else yearRef.year
                        },
                ) {
                    Text(
                        text = yearRef.year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (expandedYear == yearRef.year) {
                    yearRef.months.forEach { monthRef ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable {
                                    scope.launch { gridState.scrollToItem(monthRef.itemIndex) }
                                },
                        ) {
                            Text(
                                text = TimeFormat.monthShortName(monthRef.month),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
