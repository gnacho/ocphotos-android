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
    fun `toggle favorite calls api`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dao = FakeAssetDao()
        dao.items[1] = assetDto(1, 100).toEntity()
        val api = FakePhotosApi()
        api.assetTagsResult = listOf("pets")
        val vm = ViewerViewModel(buildRepository(api, dao), 1)

        advanceUntilIdle()
        val asset = vm.assets.value.first { it.id == 1L }

        vm.loadTags(asset)
        advanceUntilIdle()
        assertEquals(listOf("pets"), vm.tags.value)

        vm.toggleFavorite(asset)
        advanceUntilIdle()

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
        val asset = vm.assets.value.first()

        vm.addTag(asset, " beach ")
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
        val asset = vm.assets.value.first()

        vm.loadTags(asset)
        advanceUntilIdle()
        vm.removeTag(asset, "pets")
        advanceUntilIdle()

        assertEquals(listOf("beach"), vm.tags.value)
        assertEquals(listOf(1L to "pets"), api.removeTagCalls)
        assertTrue(api.addTagCalls.isEmpty())
        Dispatchers.resetMain()
    }
}
