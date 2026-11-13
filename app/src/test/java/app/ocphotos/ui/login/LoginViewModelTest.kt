package app.ocphotos.ui.login

import app.ocphotos.testutil.FakePhotosApi
import app.ocphotos.testutil.FakeSessionStore
import app.ocphotos.testutil.buildRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @Test
    fun `successful login persists the session`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val api = FakePhotosApi()
        val sessionStore = FakeSessionStore()
        val vm = LoginViewModel(buildRepository(api, sessionStore = sessionStore))

        vm.onServerUrlChange("https://drive.example.com")
        vm.onUsernameChange("nacho")
        vm.onTokenChange("one two three four five six")
        vm.login()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals("nacho", sessionStore.saved?.username)
        Dispatchers.resetMain()
    }

    @Test
    fun `failed login does not persist the session`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val api = FakePhotosApi().apply { failStats = true }
        val sessionStore = FakeSessionStore()
        val vm = LoginViewModel(buildRepository(api, sessionStore = sessionStore))

        vm.onServerUrlChange("https://drive.example.com")
        vm.onUsernameChange("nacho")
        vm.onTokenChange("bad")
        vm.login()
        advanceUntilIdle()

        assertEquals("unauthorized", vm.state.value.error)
        assertNull(sessionStore.saved)
        Dispatchers.resetMain()
    }

    @Test
    fun `reset clears the form after a successful login`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val api = FakePhotosApi()
        val sessionStore = FakeSessionStore()
        val vm = LoginViewModel(buildRepository(api, sessionStore = sessionStore))

        vm.onServerUrlChange("https://drive.example.com")
        vm.onUsernameChange("nacho")
        vm.onTokenChange("one two three four five six")
        vm.login()
        advanceUntilIdle()

        vm.reset()

        assertEquals(LoginUiState(), vm.state.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `empty fields do not call the api`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val api = FakePhotosApi()
        val vm = LoginViewModel(buildRepository(api))

        vm.login()
        advanceUntilIdle()

        assertEquals("Fill server, username and token", vm.state.value.error)
        Dispatchers.resetMain()
    }
}
