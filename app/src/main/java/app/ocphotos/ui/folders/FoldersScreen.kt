package app.ocphotos.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.remote.dto.FolderDto
import app.ocphotos.ui.components.AssetThumb
import app.ocphotos.ui.components.StatusBox
import androidx.compose.ui.res.stringResource
import app.ocphotos.R
import app.ocphotos.util.UiError

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoldersScreen(
    folders: List<FolderDto>,
    assets: List<AssetEntity>,
    loading: Boolean,
    error: UiError?,
    canGoUp: Boolean,
    onOpenFolder: (String) -> Unit,
    onUp: () -> Unit,
    onOpenAsset: (Long) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        if (canGoUp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_up))
                }
                Text(stringResource(R.string.folders_parent), style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (loading && folders.isEmpty() && assets.isEmpty()) {
            StatusBox(loading = true, error = null, empty = false)
            return@Column
        }
        if (folders.isEmpty() && assets.isEmpty()) {
            StatusBox(
                loading = false,
                error = error,
                empty = error == null,
                emptyLabel = R.string.empty_folder,
            )
            return@Column
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(folders, key = { it.path }) { folder ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFolder(folder.name) }
                        .padding(16.dp),
                ) {
                    Icon(Icons.Filled.Folder, contentDescription = null)
                    Text(folder.name, Modifier.weight(1f).padding(start = 12.dp))
                    Text("${folder.count}")
                }
                HorizontalDivider()
            }
            if (assets.isNotEmpty()) {
                item(key = "assets") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(4.dp),
                    ) {
                        assets.forEach { asset ->
                            AssetThumb(
                                asset = asset,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clickable { onOpenAsset(asset.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
