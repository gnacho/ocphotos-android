package app.ocphotos.data.repo

import app.ocphotos.testutil.FakePhotosApi
import app.ocphotos.testutil.assetDto
import app.ocphotos.testutil.buildRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosRepositoryTest {

    @Test
    fun `sync runs are serialized`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val api = FakePhotosApi()
        api.all.addAll(listOf(assetDto(1, 100), assetDto(2, 90)))
        var inFlight = 0
        var maxInFlight = 0
        api.onAssets = {
            inFlight++
            maxInFlight = maxOf(maxInFlight, inFlight)
            delay(10)
            inFlight--
        }
        val repo = buildRepository(api)

        val j1 = launch { repo.sync() }
        val j2 = launch { repo.sync() }
        j1.join()
        j2.join()

        // The Mutex must prevent overlapping assets() calls.
        assertEquals(1, maxInFlight)
        Dispatchers.resetMain()
    }
}
