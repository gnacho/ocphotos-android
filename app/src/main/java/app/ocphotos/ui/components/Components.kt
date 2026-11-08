package app.ocphotos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.repo.PhotosRepository
import coil3.compose.AsyncImage

// CompositionLocal so composables can build authenticated image URLs via the repo.
val LocalRepository = staticCompositionLocalOf<PhotosRepository> {
    error("PhotosRepository not provided")
}

@Composable
fun AssetThumb(
    asset: AssetEntity,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val repo = LocalRepository.current
    AsyncImage(
        model = repo.thumbUrl(asset.id),
        contentDescription = asset.filename,
        modifier = modifier,
        contentScale = contentScale,
    )
}

@Composable
fun PhotoGrid(
    assets: List<AssetEntity>,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    showVideoBadge: Boolean = true,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(assets, key = { it.id }) { asset ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onOpen(asset.id) },
            ) {
                AssetThumb(asset = asset, modifier = Modifier.fillMaxSize())
                if (showVideoBadge && asset.mediaType == "video") {
                    Text(
                        text = "\u25B6",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.BottomEnd),
                    )
                }
                if (asset.favorite) {
                    Text(
                        text = "\u2665",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBox(
    loading: Boolean,
    error: String?,
    empty: Boolean,
    emptyLabel: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
            empty -> Text(text = emptyLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
