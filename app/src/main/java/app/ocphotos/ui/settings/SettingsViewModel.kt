package app.ocphotos.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.ocphotos.data.settings.AppSettings
import app.ocphotos.data.settings.SettingsStore
import app.ocphotos.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {

    val settings: StateFlow<AppSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { store.setTheme(mode) }
    }

    fun setSyncInterval(minutes: Int) {
        viewModelScope.launch { store.setSyncInterval(minutes) }
    }

    companion object {
        fun factory(store: SettingsStore) = viewModelFactory {
            initializer { SettingsViewModel(store) }
        }
    }
}
