package app.ocphotos.testutil

import app.ocphotos.data.local.AlbumDao
import app.ocphotos.data.local.AlbumEntity
import app.ocphotos.data.local.AssetDao
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.remote.PhotosApi
import app.ocphotos.data.remote.dto.AddTagBody
import app.ocphotos.data.remote.dto.AddToAlbumBody
import app.ocphotos.data.remote.dto.AlbumDto
import app.ocphotos.data.remote.dto.AlbumsResponse
import app.ocphotos.data.remote.dto.ArchiveBody
import app.ocphotos.data.remote.dto.AssetDto
import app.ocphotos.data.remote.dto.AssetTagsResponse
import app.ocphotos.data.remote.dto.AssetsResponse
import app.ocphotos.data.remote.dto.CalendarResponse
import app.ocphotos.data.remote.dto.CreateAlbumResponse
import app.ocphotos.data.remote.dto.DuplicatesResponse
import app.ocphotos.data.remote.dto.FavoriteBody
import app.ocphotos.data.remote.dto.FoldersResponse
import app.ocphotos.data.remote.dto.GeoResponse
import app.ocphotos.data.remote.dto.MemoriesResponse
import app.ocphotos.data.remote.dto.NameBody
import app.ocphotos.data.remote.dto.OnThisDayResponse
import app.ocphotos.data.remote.dto.PlacesResponse
import app.ocphotos.data.remote.dto.RescanResponse
import app.ocphotos.data.remote.dto.ResolveResponse
import app.ocphotos.data.remote.dto.StatsResponse
import app.ocphotos.data.remote.dto.TagDto
import app.ocphotos.data.remote.dto.TagsResponse
import app.ocphotos.data.remote.dto.VideoUrlResponse
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.data.session.Session
import app.ocphotos.data.session.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient

class FakeAssetDao : AssetDao {
    val items = LinkedHashMap<Long, AssetEntity>()
    var clearCalls = 0
    val deleteMissingCalls = mutableListOf<List<Long>>()
    var lastUpsert: List<AssetEntity> = emptyList()
    val favoriteSetCalls = mutableListOf<Pair<Long, Boolean>>()
    val archivedSetCalls = mutableListOf<Pair<Long, Boolean>>()

    override suspend fun upsertAll(assets: List<AssetEntity>) {
        lastUpsert = assets
        assets.forEach { items[it.id] = it }
    }

    override fun timeline(): Flow<List<AssetEntity>> =
        MutableStateFlow(items.values.sortedWith(compareByDescending<AssetEntity> { it.takenAt }.thenByDescending { it.id }))

    override fun favorites(): Flow<List<AssetEntity>> = MutableStateFlow(items.values.filter { it.favorite })

    override fun archived(): Flow<List<AssetEntity>> = MutableStateFlow(items.values.filter { it.archived })

    override suspend fun withGeo(): List<AssetEntity> = items.values.filter { it.lat != null }

    override suspend fun byId(id: Long): AssetEntity? = items[id]

    override fun search(q: String): Flow<List<AssetEntity>> = MutableStateFlow(emptyList())

    override fun count(): Flow<Int> = MutableStateFlow(items.size)

    override fun favoriteCount(): Flow<Int> = MutableStateFlow(items.values.count { it.favorite })

    override suspend fun setFavorite(id: Long, value: Boolean) {
        favoriteSetCalls += id to value
        items[id]?.let { items[id] = it.copy(favorite = value) }
    }

    override suspend fun setArchived(id: Long, value: Boolean) {
        archivedSetCalls += id to value
        items[id]?.let { items[id] = it.copy(archived = value) }
    }

    override suspend fun deleteMissing(ids: List<Long>) {
        deleteMissingCalls += ids
        items.keys.retainAll(ids.toSet())
    }

    override suspend fun clear() {
        clearCalls++
        items.clear()
    }
}

class FakeAlbumDao : AlbumDao {
    val items = LinkedHashMap<Long, AlbumEntity>()
    var clearCalls = 0

    override suspend fun upsertAll(albums: List<AlbumEntity>) {
        albums.forEach { items[it.id] = it }
    }

    override fun all(): Flow<List<AlbumEntity>> =
        MutableStateFlow(items.values.sortedWith(compareByDescending<AlbumEntity> { it.createdAt }))

    override suspend fun byId(id: Long): AlbumEntity? = items[id]

    override suspend fun clear() {
        clearCalls++
        items.clear()
    }
}

class FakeSessionStore : SessionStore {
    private val _session = MutableStateFlow<Session?>(null)
    override val session: Flow<Session?> = _session
    var saved: Session? = null
        private set

    override suspend fun save(session: Session) {
        saved = session
        _session.value = session
    }

    override suspend fun clear() {
        _session.value = null
    }
}

class FakePhotosApi : PhotosApi {
    val all = mutableListOf<AssetDto>()
    var failStats = false
    var failAssets = false

    var tagsResult = listOf<TagDto>()
    var assetTagsResult = listOf<String>()
    var albumsResult = listOf<AlbumDto>()

    val favoriteCalls = mutableListOf<Pair<Long, Boolean>>()
    val archiveCalls = mutableListOf<Pair<Long, Boolean>>()
    val addTagCalls = mutableListOf<Pair<Long, String>>()
    val removeTagCalls = mutableListOf<Pair<Long, String>>()

    override suspend fun stats(): StatsResponse {
        if (failStats) throw RuntimeException("unauthorized")
        return StatsResponse(assets = all.size.toLong())
    }

    override suspend fun assets(
        beforeTaken: Long?,
        beforeId: Long?,
        limit: Int?,
        favorites: Int?,
        archived: Int?,
        q: String?,
    ): AssetsResponse {
        if (failAssets) throw RuntimeException("boom")
        var result = all.toList()
        if (beforeTaken != null && beforeId != null) {
            result = result.filter {
                it.takenAt < beforeTaken || (it.takenAt == beforeTaken && it.id < beforeId)
            }
        }
        return AssetsResponse(result.take(limit ?: 500))
    }

    override suspend fun resolve(driveAliasAndItem: String): ResolveResponse = ResolveResponse(null)

    override suspend fun favorite(id: Long, body: FavoriteBody) {
        favoriteCalls += id to body.favorite
    }

    override suspend fun archive(id: Long, body: ArchiveBody) {
        archiveCalls += id to body.archived
    }

    override suspend fun duplicates(): DuplicatesResponse = DuplicatesResponse()

    override suspend fun videoUrl(id: Long): VideoUrlResponse = VideoUrlResponse("/ocphotos-api/api/video/$id")

    override suspend fun onThisDay(days: Int?): OnThisDayResponse = OnThisDayResponse()

    override suspend fun highlights(): MemoriesResponse = MemoriesResponse()

    override suspend fun calendar(): CalendarResponse = CalendarResponse()

    override suspend fun geo(): GeoResponse = GeoResponse()

    override suspend fun places(): PlacesResponse = PlacesResponse()

    override suspend fun tags(): TagsResponse = TagsResponse(tagsResult)

    override suspend fun tagAssets(tag: String): AssetsResponse = AssetsResponse()

    override suspend fun assetTags(id: Long): AssetTagsResponse = AssetTagsResponse(assetTagsResult)

    override suspend fun addTag(id: Long, body: AddTagBody) {
        addTagCalls += id to body.tag
    }

    override suspend fun removeTag(id: Long, tag: String) {
        removeTagCalls += id to tag
    }

    override suspend fun folders(path: String?): FoldersResponse = FoldersResponse()

    override suspend fun albums(): AlbumsResponse = AlbumsResponse(albumsResult)

    override suspend fun createAlbum(body: NameBody): CreateAlbumResponse = CreateAlbumResponse(1)

    override suspend fun renameAlbum(id: Long, body: NameBody) {}

    override suspend fun deleteAlbum(id: Long) {}

    override suspend fun albumAssets(id: Long): AssetsResponse = AssetsResponse()

    override suspend fun addToAlbum(id: Long, body: AddToAlbumBody) {}

    override suspend fun removeFromAlbum(id: Long, assetId: Long) {}

    override suspend fun rescan(): RescanResponse = RescanResponse("ok")
}

fun buildRepository(
    api: FakePhotosApi = FakePhotosApi(),
    assetDao: FakeAssetDao = FakeAssetDao(),
    albumDao: FakeAlbumDao = FakeAlbumDao(),
    sessionStore: FakeSessionStore = FakeSessionStore(),
): PhotosRepository {
    val repo = PhotosRepository(
        sessionStore = sessionStore,
        assetDao = assetDao,
        albumDao = albumDao,
        apiFactory = { api },
        httpClient = OkHttpClient(),
        onCredentialsChange = {},
    )
    // Bind a session so requireApi() resolves to the fake (mirrors app startup).
    repo.bind(Session("https://test.example.com", "user", "token"))
    return repo
}

fun assetDto(id: Long, takenAt: Long, favorite: Boolean = false, archived: Boolean = false): AssetDto =
    AssetDto(
        id = id,
        path = "/dav/spaces/p/x$id.jpg",
        filename = "x$id.jpg",
        mediaType = "image",
        takenAt = takenAt,
        width = 100,
        height = 100,
        size = 1000,
        favorite = favorite,
        archived = archived,
    )

fun albumDto(id: Long, name: String): AlbumDto = AlbumDto(id = id, name = name, count = 0, createdAt = id)
