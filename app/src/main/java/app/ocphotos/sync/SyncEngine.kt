package app.ocphotos.sync

import app.ocphotos.data.toEntity
import app.ocphotos.data.local.AlbumDao
import app.ocphotos.data.local.AssetDao
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.remote.PhotosApi

// Pure sync logic (testable with fakes). Walks the keyset-paginated /assets
// endpoint (before_taken/before_id, order takenAt DESC, id DESC) until exhausted,
// then reconciles the local Room mirror.
class SyncEngine(
    private val api: PhotosApi,
    private val assetDao: AssetDao,
    private val albumDao: AlbumDao,
) {
    suspend fun fullSync(limit: Int = 500): Int {
        val all = mutableListOf<AssetEntity>()
        var beforeTaken: Long? = null
        var beforeId: Long? = null

        while (true) {
            val resp = api.assets(beforeTaken = beforeTaken, beforeId = beforeId, limit = limit)
            val batch = resp.assets.map { it.toEntity() }
            if (batch.isEmpty()) break
            all += batch
            val last = batch.last()
            beforeTaken = last.takenAt
            beforeId = last.id
            if (batch.size < limit) break
        }

        // Empty-index guard: `DELETE ... WHERE id NOT IN ()` would delete every row.
        if (all.isEmpty()) {
            assetDao.clear()
        } else {
            assetDao.upsertAll(all)
            assetDao.deleteMissing(all.map { it.id })
        }
        return all.size
    }

    suspend fun refreshAlbums() {
        albumDao.upsertAll(api.albums().albums.map { it.toEntity() })
    }
}
