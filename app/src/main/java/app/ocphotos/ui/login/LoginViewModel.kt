package app.ocphotos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.data.session.Session
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val token: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(private val repository: PhotosRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onServerUrlChange(v: String) = _state.update { it.copy(serverUrl = v) }
    fun onUsernameChange(v: String) = _state.update { it.copy(username = v) }
    fun onTokenChange(v: String) = _state.update { it.copy(token = v) }

    fun login() {
        val s = _state.value
        if (s.serverUrl.isBlank() || s.username.isBlank() || s.token.isBlank()) {
            _state.update { it.copy(error = "Fill server, username and token") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                // Validate credentials with GET /stats before persisting the session.
                val session = Session(s.serverUrl, s.username, s.token)
                repository.bind(session)
                repository.stats()
                repository.login(session)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                repository.bind(null)
                _state.update { it.copy(loading = false, error = app.ocphotos.util.friendlyMessage(e)) }
            }
        }
    }

    companion object {
        fun factory(repository: PhotosRepository) = viewModelFactory {
            initializer { LoginViewModel(repository) }
        }
    }
}
