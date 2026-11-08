package app.ocphotos.ui.timeline

import app.ocphotos.testutil.FakePhotosApi
import app.ocphotos.testutil.buildRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @Test
    fun `sync failure sets an error instead of crashing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val api = FakePhotosApi().apply { failAssets = true }
        val vm = TimelineViewModel(buildRepository(api))

        advanceUntilIdle()

        assertEquals("boom", vm.syncError.value)
        assertEquals(false, vm.syncing.value)
        Dispatchers.resetMain()
    }
}
