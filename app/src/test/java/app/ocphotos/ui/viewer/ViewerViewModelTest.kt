package app.ocphotos.ui.viewer

import app.ocphotos.data.toEntity
import app.ocphotos.testutil.FakePhotosApi
import app.ocphotos.testutil.FakeAssetDao
import app.ocphotos.testutil.assetDto
import app.ocphotos.testutil.buildRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewerViewModelTest {

    @Test
    fun `toggle favorite updates state and calls api`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dao = FakeAssetDao()
        dao.items[1] = assetDto(1, 100).toEntity()
        val api = FakePhotosApi()
        api.assetTagsResult = listOf("pets")
        val vm = ViewerViewModel(buildRepository(api, dao), 1)

        advanceUntilIdle()
        assertEquals(false, vm.asset.value?.favorite)
        assertEquals(listOf("pets"), vm.tags.value)

        vm.toggleFavorite()
        advanceUntilIdle()

        assertEquals(true, vm.asset.value?.favorite)
        assertEquals(listOf(1L to true), api.favoriteCalls)
        Dispatchers.resetMain()
    }

    @Test
    fun `add tag calls api and updates list`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dao = FakeAssetDao()
        dao.items[1] = assetDto(1, 100).toEntity()
        val api = FakePhotosApi()
        val vm = ViewerViewModel(buildRepository(api, dao), 1)

        advanceUntilIdle()
        vm.addTag(" beach ")
        advanceUntilIdle()

        assertEquals(listOf("beach"), vm.tags.value)
        assertEquals(listOf(1L to "beach"), api.addTagCalls)
        Dispatchers.resetMain()
    }

    @Test
    fun `remove tag calls api and updates list`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dao = FakeAssetDao()
        dao.items[1] = assetDto(1, 100).toEntity()
        val api = FakePhotosApi()
        api.assetTagsResult = listOf("pets", "beach")
        val vm = ViewerViewModel(buildRepository(api, dao), 1)

        advanceUntilIdle()
        vm.removeTag("pets")
        advanceUntilIdle()

        assertEquals(listOf("beach"), vm.tags.value)
        assertEquals(listOf(1L to "pets"), api.removeTagCalls)
        assertTrue(api.addTagCalls.isEmpty())
        Dispatchers.resetMain()
    }
}
