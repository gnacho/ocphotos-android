package app.ocphotos.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.ocphotos.data.local.AlbumEntity
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.ui.components.LocalRepository
import app.ocphotos.util.ByteFormat
import app.ocphotos.util.TimeFormat
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ViewerScreen(
    viewModel: ViewerViewModel,
    onClose: () -> Unit,
) {
    val assets by viewModel.assets.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val repo = LocalRepository.current
    val origin by repo.serverOrigin.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    if (assets.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Photo not found", color = Color.White)
        }
        return
    }

    val startIndex = assets.indexOfFirst { it.id == viewModel.assetId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { assets.size })
    val currentAsset = assets[pagerState.currentPage]

    var showTags by remember { mutableStateOf(false) }
    var showAlbums by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offset = Offset.Zero
        viewModel.loadTags(assets[pagerState.currentPage])
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val asset = assets[page]
            if (asset.mediaType == "video") {
                VideoPlayer(viewModel = viewModel, asset = asset, origin = origin)
            } else {
                AsyncImage(
                    model = origin?.let { repo.originalUrl(asset.id) },
                    contentDescription = asset.filename,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                        )
                        .pointerInput(Unit) {
                            // Pinch-to-zoom only on two fingers; one finger stays for
                            // the pager's swipe so zoom never blocks navigation.
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                do {
                                    val event = awaitPointerEvent()
                                    val pressed = event.changes.count { it.pressed }
                                    if (pressed >= 2) {
                                        val zoom = event.calculateZoom()
                                        val pan = event.calculatePan()
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        offset = if (scale > 1f) offset + pan else Offset.Zero
                                        event.changes.forEach { it.consume() }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        },
                    contentScale = ContentScale.Fit,
                )
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
            }
            Text(
                text = currentAsset.filename,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = { viewModel.toggleFavorite(currentAsset) }) {
                Icon(
                    if (currentAsset.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (currentAsset.favorite) Color(0xFFFF4081) else Color.White,
                )
            }
            IconButton(onClick = { viewModel.toggleArchived(currentAsset) }) {
                Icon(Icons.Filled.Archive, contentDescription = "Archive", tint = Color.White)
            }
            IconButton(onClick = { showTags = true }) {
                Icon(Icons.Filled.Sell, contentDescription = "Tags", tint = Color.White)
            }
            IconButton(onClick = { showAlbums = true }) {
                Icon(Icons.Filled.PhotoAlbum, contentDescription = "Add to album", tint = Color.White)
            }
            IconButton(onClick = {
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                                ?: context.cacheDir
                            repo.downloadOriginal(currentAsset.id, File(dir, currentAsset.filename))
                        }
                    }
                    val msg = result.fold(
                        onSuccess = { "Saved to ${it.parent}" },
                        onFailure = { "Download failed: ${app.ocphotos.util.friendlyMessage(it)}" },
                    )
                    snackbarHostState.showSnackbar(msg)
                }
            }) {
                Icon(Icons.Filled.FileDownload, contentDescription = "Download", tint = Color.White)
            }
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White)
            }
        }

        // Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = TimeFormat.formatShortDate(currentAsset.takenAt) +
                    if (currentAsset.camera != null) " \u00B7 ${currentAsset.camera}" else "",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 104.dp),
        )
    }

    if (showTags) {
        TagDialog(
            tags = tags,
            onAdd = { viewModel.addTag(currentAsset, it) },
            onRemove = { viewModel.removeTag(currentAsset, it) },
            onDismiss = { showTags = false },
        )
    }

    if (showAlbums) {
        AlbumPickerDialog(
            albums = albums,
            onPick = { id ->
                viewModel.addToAlbum(currentAsset, id)
                showAlbums = false
            },
            onDismiss = { showAlbums = false },
        )
    }

    if (showInfo) {
        InfoDialog(asset = currentAsset, onDismiss = { showInfo = false })
    }
}

@Composable
private fun TagDialog(
    tags: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newTag by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tags") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                tags.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tag, Modifier.weight(1f))
                        TextButton(onClick = { onRemove(tag) }) { Text("Remove") }
                    }
                    HorizontalDivider()
                }
                if (tags.isEmpty()) Text("No tags")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = { Text("New tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(newTag)
                newTag = ""
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun AlbumPickerDialog(
    albums: List<AlbumEntity>,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to album") },
        text = {
            if (albums.isEmpty()) {
                Text("No albums")
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    albums.forEach { album ->
                        Text(
                            text = "${album.name} (${album.count})",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(album.id) }
                                .padding(vertical = 10.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun InfoDialog(asset: AssetEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(asset.filename) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                InfoRow("Date", TimeFormat.formatFullDate(asset.takenAt))
                asset.camera?.let { InfoRow("Camera", it) }
                asset.lens?.let { InfoRow("Lens", it) }
                asset.iso?.let { InfoRow("ISO", it.toString()) }
                asset.aperture?.let { InfoRow("Aperture", it) }
                asset.shutter?.let { InfoRow("Shutter", it) }
                asset.focal?.let { InfoRow("Focal", it) }
                if (asset.width > 0) InfoRow("Dimensions", "${asset.width} x ${asset.height}")
                InfoRow("Size", ByteFormat.human(asset.size))
                if (asset.lat != null && asset.lon != null) {
                    InfoRow("Location", "%.5f, %.5f".format(asset.lat, asset.lon))
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(value, Modifier.weight(1f))
    }
}

// In-app video playback: ExoPlayer (Media3) fed with the signed stream URL.
@Composable
private fun VideoPlayer(viewModel: ViewerViewModel, asset: AssetEntity, origin: String?) {
    val context = LocalContext.current
    val player = remember(asset.id) { ExoPlayer.Builder(context).build() }

    LaunchedEffect(asset.id, origin) {
        val url = viewModel.videoUrl(asset)
        if (url != null) {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = true
        }
    }

    DisposableEffect(asset.id) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply { this.player = player }
        },
        modifier = Modifier.fillMaxSize(),
    )
}
