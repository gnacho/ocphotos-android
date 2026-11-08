package app.ocphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.ocphotos.data.settings.AppSettings
import app.ocphotos.data.settings.ThemeMode
import app.ocphotos.ui.albums.AlbumsScreen
import app.ocphotos.ui.albums.AlbumsViewModel
import app.ocphotos.ui.components.AssetGridScreen
import app.ocphotos.ui.components.LocalRepository
import app.ocphotos.ui.duplicates.DuplicatesScreen
import app.ocphotos.ui.duplicates.DuplicatesViewModel
import app.ocphotos.ui.explore.ExploreScreen
import app.ocphotos.ui.explore.ExploreViewModel
import app.ocphotos.ui.folders.FoldersScreen
import app.ocphotos.ui.folders.FoldersViewModel
import app.ocphotos.ui.list.FlowAssetsViewModel
import app.ocphotos.ui.list.LoadAssetsViewModel
import app.ocphotos.ui.login.LoginScreen
import app.ocphotos.ui.login.LoginViewModel
import app.ocphotos.ui.map.MapScreen
import app.ocphotos.ui.map.MapViewModel
import app.ocphotos.ui.memories.MemoriesScreen
import app.ocphotos.ui.memories.MemoriesViewModel
import app.ocphotos.ui.settings.SettingsScreen
import app.ocphotos.ui.settings.SettingsViewModel
import app.ocphotos.ui.tags.TagsScreen
import app.ocphotos.ui.tags.TagsViewModel
import app.ocphotos.ui.theme.OcphotosTheme
import app.ocphotos.ui.timeline.TimelineScreen
import app.ocphotos.ui.timeline.TimelineViewModel
import app.ocphotos.ui.viewer.ViewerScreen
import app.ocphotos.ui.viewer.ViewerViewModel
import kotlinx.coroutines.launch

private data class Section(val route: String, val label: String, val icon: ImageVector)
private data class SectionGroup(val header: String, val items: List<Section>)

private val sectionGroups = listOf(
    SectionGroup("Library", listOf(
        Section("timeline", "Photos", Icons.Filled.PhotoLibrary),
        Section("favorites", "Favorites", Icons.Filled.Favorite),
        Section("archive", "Archive", Icons.Filled.Archive),
    )),
    SectionGroup("Organize", listOf(
        Section("tags", "Tags", Icons.Filled.Sell),
        Section("albums", "Albums", Icons.Filled.PhotoAlbum),
        Section("folders", "Folders", Icons.Filled.Folder),
        Section("duplicates", "Duplicates", Icons.Filled.ContentCopy),
    )),
    SectionGroup("Discover", listOf(
        Section("memories", "Memories", Icons.Filled.CalendarMonth),
        Section("explore", "Explore", Icons.Filled.Search),
        Section("map", "Map", Icons.Filled.Map),
    )),
)

private val sections = sectionGroups.flatMap { it.items }

private fun titleFor(route: String?): String = when {
    route == null -> "ocphotos"
    route.startsWith("viewer") -> ""
    route.startsWith("tag/") -> "Tag"
    route.startsWith("album/") -> "Album"
    route == "settings" -> "Settings"
    else -> sections.firstOrNull { it.route == route }?.label ?: "ocphotos"
}

class MainActivity : ComponentActivity() {

    private val repository get() = (application as PhotosApplication).container.repository
    private val settingsStore get() = (application as PhotosApplication).container.settingsStore

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(repository)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.factory(settingsStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val darkTheme = when (settings.theme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            OcphotosTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalRepository provides repository) {
                    val session by repository.session.collectAsStateWithLifecycle(initialValue = null)
                    if (session == null) {
                        val state by loginViewModel.state.collectAsStateWithLifecycle()
                        LoginScreen(
                            state = state,
                            onServerUrlChange = loginViewModel::onServerUrlChange,
                            onUsernameChange = loginViewModel::onUsernameChange,
                            onTokenChange = loginViewModel::onTokenChange,
                            onLogin = loginViewModel::login,
                        )
                    } else {
                        AppNavHost()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppNavHost() {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route
        val isViewer = currentRoute?.startsWith("viewer") == true

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isViewer,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        "ocphotos",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                    HorizontalDivider()
                    sectionGroups.forEach { group ->
                        Text(
                            group.header,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 28.dp, top = 12.dp, bottom = 4.dp),
                        )
                        group.items.forEach { section ->
                            NavigationDrawerItem(
                                label = { Text(section.label) },
                                icon = { Icon(section.icon, contentDescription = null) },
                                selected = currentRoute == section.route,
                                onClick = {
                                    scope.launch {
                                        navController.navigate(section.route) {
                                            popUpTo("timeline") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                        drawerState.close()
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        selected = currentRoute == "settings",
                        onClick = {
                            scope.launch {
                                navController.navigate("settings") {
                                    popUpTo("timeline") { inclusive = false }
                                    launchSingleTop = true
                                }
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                repository.logout()
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            },
        ) {
            Scaffold(
                topBar = {
                    if (!isViewer) {
                        TopAppBar(
                            title = { Text(titleFor(currentRoute)) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                                }
                            },
                        )
                    }
                },
            ) { padding ->
                val contentPadding = if (isViewer) PaddingValues(0.dp) else padding
                NavHost(
                    navController = navController,
                    startDestination = "timeline",
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                ) {
                    composable("timeline") {
                        val vm: TimelineViewModel = viewModel(factory = TimelineViewModel.factory(repository))
                        val assets by vm.assets.collectAsStateWithLifecycle()
                        val syncing by vm.syncing.collectAsStateWithLifecycle()
                        val syncError by vm.syncError.collectAsStateWithLifecycle()
                        TimelineScreen(
                            assets = assets,
                            syncing = syncing,
                            syncError = syncError,
                            onOpen = { navController.navigate("viewer/$it") },
                            onToggleFavorite = vm::toggleFavorite,
                            onSync = vm::sync,
                            onRescan = vm::rescan,
                        )
                    }
                    composable("memories") {
                        val vm: MemoriesViewModel = viewModel(factory = MemoriesViewModel.factory(repository))
                        MemoriesScreen(vm) { navController.navigate("viewer/$it") }
                    }
                    composable("explore") {
                        val vm: ExploreViewModel = viewModel(factory = ExploreViewModel.factory(repository))
                        ExploreScreen(vm) { navController.navigate("viewer/$it") }
                    }
                    composable("map") {
                        val vm: MapViewModel = viewModel(factory = MapViewModel.factory(repository))
                        MapScreen(vm) { navController.navigate("viewer/$it") }
                    }
                    composable("favorites") {
                        val vm: FlowAssetsViewModel = viewModel(
                            factory = viewModelFactory { initializer { FlowAssetsViewModel(repository.favorites) } },
                        )
                        val assets by vm.assets.collectAsStateWithLifecycle()
                        AssetGridScreen(
                            assets = assets,
                            loading = false,
                            error = null,
                            emptyLabel = "No favorites",
                            onOpen = { navController.navigate("viewer/$it") },
                        )
                    }
                    composable("archive") {
                        val vm: FlowAssetsViewModel = viewModel(
                            factory = viewModelFactory { initializer { FlowAssetsViewModel(repository.archived) } },
                        )
                        val assets by vm.assets.collectAsStateWithLifecycle()
                        AssetGridScreen(
                            assets = assets,
                            loading = false,
                            error = null,
                            emptyLabel = "No archived photos",
                            onOpen = { navController.navigate("viewer/$it") },
                        )
                    }
                    composable("tags") {
                        val vm: TagsViewModel = viewModel(factory = TagsViewModel.factory(repository))
                        val tags by vm.tags.collectAsStateWithLifecycle()
                        val loading by vm.loading.collectAsStateWithLifecycle()
                        val error by vm.error.collectAsStateWithLifecycle()
                        TagsScreen(tags, loading, error) { navController.navigate("tag/$it") }
                    }
                    composable(
                        "tag/{tag}",
                        arguments = listOf(navArgument("tag") { type = NavType.StringType }),
                    ) { entry ->
                        val tag = entry.arguments?.getString("tag") ?: ""
                        val vm: LoadAssetsViewModel = viewModel(
                            key = "tag-$tag",
                            factory = viewModelFactory { initializer { LoadAssetsViewModel { repository.tagAssets(tag) } } },
                        )
                        val assets by vm.assets.collectAsStateWithLifecycle()
                        val loading by vm.loading.collectAsStateWithLifecycle()
                        val error by vm.error.collectAsStateWithLifecycle()
                        AssetGridScreen(
                            assets = assets,
                            loading = loading,
                            error = error,
                            emptyLabel = "No photos with this tag",
                            onOpen = { navController.navigate("viewer/$it") },
                        )
                    }
                    composable("albums") {
                        val vm: AlbumsViewModel = viewModel(factory = AlbumsViewModel.factory(repository))
                        val albums by vm.albums.collectAsStateWithLifecycle()
                        AlbumsScreen(
                            albums = albums,
                            onOpen = { navController.navigate("album/$it") },
                            onCreate = vm::create,
                            onRename = vm::rename,
                            onDelete = vm::delete,
                        )
                    }
                    composable(
                        "album/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType }),
                    ) { entry ->
                        val id = entry.arguments?.getLong("id") ?: 0L
                        val vm: LoadAssetsViewModel = viewModel(
                            key = "album-$id",
                            factory = viewModelFactory { initializer { LoadAssetsViewModel { repository.albumAssets(id) } } },
                        )
                        val assets by vm.assets.collectAsStateWithLifecycle()
                        val loading by vm.loading.collectAsStateWithLifecycle()
                        val error by vm.error.collectAsStateWithLifecycle()
                        AssetGridScreen(
                            assets = assets,
                            loading = loading,
                            error = error,
                            emptyLabel = "Empty album",
                            onOpen = { navController.navigate("viewer/$it") },
                        )
                    }
                    composable("folders") {
                        val vm: FoldersViewModel = viewModel(factory = FoldersViewModel.factory(repository))
                        val folders by vm.folders.collectAsStateWithLifecycle()
                        val assets by vm.assets.collectAsStateWithLifecycle()
                        val loading by vm.loading.collectAsStateWithLifecycle()
                        val error by vm.error.collectAsStateWithLifecycle()
                        val stack by vm.pathStack.collectAsStateWithLifecycle()
                        FoldersScreen(
                            folders = folders,
                            assets = assets,
                            loading = loading,
                            error = error,
                            canGoUp = stack.size > 1,
                            onOpenFolder = vm::openFolder,
                            onUp = vm::up,
                            onOpenAsset = { navController.navigate("viewer/$it") },
                        )
                    }
                    composable("duplicates") {
                        val vm: DuplicatesViewModel = viewModel(factory = DuplicatesViewModel.factory(repository))
                        DuplicatesScreen(vm) { navController.navigate("viewer/$it") }
                    }
                    composable("settings") {
                        val settings by settingsViewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
                        SettingsScreen(
                            settings = settings,
                            onTheme = settingsViewModel::setTheme,
                            onInterval = settingsViewModel::setSyncInterval,
                        )
                    }
                    composable(
                        "viewer/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType }),
                    ) { entry ->
                        val id = entry.arguments?.getLong("id") ?: 0L
                        val vm: ViewerViewModel = viewModel(
                            key = "viewer-$id",
                            factory = ViewerViewModel.factory(repository, id),
                        )
                        ViewerScreen(viewModel = vm, onClose = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
