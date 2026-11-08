package app.ocphotos.sync

import app.ocphotos.data.toEntity
import app.ocphotos.testutil.FakeAlbumDao
import app.ocphotos.testutil.FakeAssetDao
import app.ocphotos.testutil.FakePhotosApi
import app.ocphotos.testutil.assetDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncEngineTest {

    private fun engine(api: FakePhotosApi, dao: FakeAssetDao): SyncEngine =
        SyncEngine(api, dao, FakeAlbumDao())

    @Test
    fun `paginates keyset until exhausted and reconciles`() = runTest {
        val api = FakePhotosApi()
        // 1200 assets, takenAt descending, id descending (like the backend order).
        api.all.addAll((1..1200).map { assetDto(id = it.toLong(), takenAt = 1201L - it) })
        val dao = FakeAssetDao()

        val count = engine(api, dao).fullSync(limit = 500)

        assertEquals(1200, count)
        assertEquals(1200, dao.items.size)
        // The full list is upserted (not just the last page).
        assertEquals(1200, dao.lastUpsert.size)
        // One reconciliation call with all ids.
        assertEquals(1, dao.deleteMissingCalls.size)
        assertEquals(1200, dao.deleteMissingCalls[0].size)
    }

    @Test
    fun `empty index clears instead of upserting`() = runTest {
        val api = FakePhotosApi()
        val dao = FakeAssetDao()

        val count = engine(api, dao).fullSync()

        assertEquals(0, count)
        assertEquals(1, dao.clearCalls)
        assertTrue(dao.deleteMissingCalls.isEmpty())
    }

    @Test
    fun `deleteMissing removes assets no longer present`() = runTest {
        val api = FakePhotosApi()
        api.all.addAll(listOf(assetDto(1, 100), assetDto(2, 90)))
        val dao = FakeAssetDao()
        // Seed a stale asset that is not in the remote index.
        dao.items[999] = assetDto(999, 5).toEntity()

        engine(api, dao).fullSync()

        assertEquals(2, dao.items.size)
        assertTrue(999L !in dao.items)
        assertEquals(1, dao.deleteMissingCalls.size)
        assertEquals(setOf(1L, 2L), dao.deleteMissingCalls[0].toSet())
    }
}
