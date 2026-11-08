package app.ocphotos.data.repo

import app.ocphotos.data.toEntity
import app.ocphotos.data.local.AlbumDao
import app.ocphotos.data.local.AlbumEntity
import app.ocphotos.data.local.AssetDao
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.remote.Credentials
import app.ocphotos.data.remote.PhotosApi
import app.ocphotos.data.remote.dto.AddTagBody
import app.ocphotos.data.remote.dto.AddToAlbumBody
import app.ocphotos.data.remote.dto.ArchiveBody
import app.ocphotos.data.remote.dto.CalendarResponse
import app.ocphotos.data.remote.dto.DuplicatesResponse
import app.ocphotos.data.remote.dto.FavoriteBody
import app.ocphotos.data.remote.dto.FoldersResponse
import app.ocphotos.data.remote.dto.MemoriesResponse
import app.ocphotos.data.remote.dto.NameBody
import app.ocphotos.data.remote.dto.PlaceDto
import app.ocphotos.data.remote.dto.StatsResponse
import app.ocphotos.data.remote.dto.TagDto
import app.ocphotos.data.session.Session
import app.ocphotos.data.session.SessionStore
import app.ocphotos.sync.SyncEngine
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class PhotosRepository(
    private val sessionStore: SessionStore,
    private val assetDao: AssetDao,
    private val albumDao: AlbumDao,
    private val apiFactory: (Session) -> PhotosApi,
    private val httpClient: OkHttpClient,
    private val onCredentialsChange: (Credentials?) -> Unit,
    private val onLogout: () -> Unit = {},
) {
    val session: Flow<Session?> = sessionStore.session

    @Volatile private var api: PhotosApi? = null
    @Volatile private var serverOrigin: String? = null

    val isBound: Boolean get() = api != null

    // Bind the current session to a Retrofit client + image credentials. Called on
    // login, logout and app start (collected from the session flow).
    fun bind(session: Session?) {
        if (session == null) {
            api = null
            serverOrigin = null
            onCredentialsChange(null)
        } else {
            api = apiFactory(session)
            serverOrigin = session.serverUrl.trimEnd('/')
            onCredentialsChange(Credentials(session.username, session.token))
        }
    }

    suspend fun login(session: Session) {
        sessionStore.save(session)
        bind(session)
    }

    suspend fun logout() {
        sessionStore.clear()
        bind(null)
        assetDao.clear()
        albumDao.clear()
        onLogout()
    }

    private fun requireApi(): PhotosApi =
        api ?: throw IllegalStateException("not logged in")

    // ---- Local reads (Room) ----
    val timeline: Flow<List<AssetEntity>> = assetDao.timeline()
    val favorites: Flow<List<AssetEntity>> = assetDao.favorites()
    val archived: Flow<List<AssetEntity>> = assetDao.archived()
    val count: Flow<Int> = assetDao.count()
    val albumsFlow: Flow<List<AlbumEntity>> = albumDao.all()
    fun search(q: String): Flow<List<AssetEntity>> = assetDao.search(q)

    suspend fun assetById(id: Long): AssetEntity? = assetDao.byId(id)

    // ---- Sync ----
    suspend fun sync(): Int = SyncEngine(requireApi(), assetDao, albumDao).fullSync()

    // ---- Stats ----
    suspend fun stats(): StatsResponse = requireApi().stats()

    // ---- Mutations ----
    suspend fun setFavorite(id: Long, value: Boolean) {
        requireApi().favorite(id, FavoriteBody(value))
        assetDao.setFavorite(id, value)
    }

    suspend fun setArchived(id: Long, value: Boolean) {
        requireApi().archive(id, ArchiveBody(value))
        assetDao.setArchived(id, value)
    }

    // ---- Tags ----
    suspend fun tags(): List<TagDto> = requireApi().tags().tags

    suspend fun tagAssets(tag: String): List<AssetEntity> =
        requireApi().tagAssets(tag).assets.map { it.toEntity() }

    suspend fun assetTags(id: Long): List<String> = requireApi().assetTags(id).tags

    suspend fun addTag(id: Long, tag: String) = requireApi().addTag(id, AddTagBody(tag))

    suspend fun removeTag(id: Long, tag: String) = requireApi().removeTag(id, tag)

    // ---- Albums ----
    suspend fun refreshAlbums(): List<AlbumEntity> {
        val list = requireApi().albums().albums.map { it.toEntity() }
        albumDao.upsertAll(list)
        return list
    }

    suspend fun createAlbum(name: String): Long = requireApi().createAlbum(NameBody(name)).id

    suspend fun renameAlbum(id: Long, name: String) =
        requireApi().renameAlbum(id, NameBody(name))

    suspend fun deleteAlbum(id: Long) {
        requireApi().deleteAlbum(id)
        albumDao.clear()
    }

    suspend fun albumAssets(id: Long): List<AssetEntity> =
        requireApi().albumAssets(id).assets.map { it.toEntity() }

    suspend fun addToAlbum(id: Long, assetIds: List<Long>) =
        requireApi().addToAlbum(id, AddToAlbumBody(assetIds))

    suspend fun removeFromAlbum(id: Long, assetId: Long) =
        requireApi().removeFromAlbum(id, assetId)

    // ---- Folders ----
    suspend fun folders(path: String? = null): FoldersResponse = requireApi().folders(path)

    // ---- Memories ----
    suspend fun onThisDay(days: Int = 3): List<AssetEntity> =
        requireApi().onThisDay(days).assets.map { it.toEntity() }

    suspend fun highlights(): MemoriesResponse = requireApi().highlights()

    suspend fun calendar(): CalendarResponse = requireApi().calendar()

    // ---- Map ----
    suspend fun places(): List<PlaceDto> = requireApi().places().places

    suspend fun geo(): List<AssetEntity> = requireApi().geo().assets.map { it.toEntity() }

    // ---- Duplicates ----
    suspend fun duplicates(): DuplicatesResponse = requireApi().duplicates()

    // ---- Remote search (Explore) ----
    suspend fun searchRemote(q: String): List<AssetEntity> =
        requireApi().assets(q = q).assets.map { it.toEntity() }

    // ---- Rescan ----
    suspend fun rescan() = requireApi().rescan()

    // ---- Image/stream URLs (resolved to the absolute server origin) ----
    fun thumbUrl(id: Long, width: Int = 400): String =
        serverOrigin?.let { "$it/ocphotos-api/api/assets/$id/thumb?w=$width" } ?: ""

    fun originalUrl(id: Long): String =
        serverOrigin?.let { "$it/ocphotos-api/api/assets/$id/original" } ?: ""

    suspend fun videoUrl(id: Long): String {
        val r = requireApi().videoUrl(id)
        return serverOrigin?.let { "$it${r.url}" } ?: ""
    }

    // Downloads the original file (authenticated) to `dest`. Returns it or throws.
    suspend fun downloadOriginal(id: Long, dest: File): File {
        val url = originalUrl(id)
        if (url.isBlank()) throw IOException("not logged in")
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
            resp.body?.byteStream()?.use { input ->
                dest.outputStream().use { input.copyTo(it) }
            } ?: throw IOException("empty body")
        }
        return dest
    }
}
