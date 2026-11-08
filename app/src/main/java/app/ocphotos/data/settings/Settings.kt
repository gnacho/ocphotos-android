package app.ocphotos.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val syncIntervalMinutes: Int = 15,
)

interface SettingsStore {
    val settings: Flow<AppSettings>
    suspend fun setTheme(mode: ThemeMode)
    suspend fun setSyncInterval(minutes: Int)
}

private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "settings")

class DataStoreSettingsStore(private val context: Context) : SettingsStore {
    private val keyTheme = stringPreferencesKey("theme")
    private val keyInterval = stringPreferencesKey("sync_interval")

    override val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[keyTheme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            syncIntervalMinutes = prefs[keyInterval]?.toIntOrNull() ?: 15,
        )
    }

    override suspend fun setTheme(mode: ThemeMode) {
        context.settingsDataStore.edit { it[keyTheme] = mode.name }
    }

    override suspend fun setSyncInterval(minutes: Int) {
        context.settingsDataStore.edit { it[keyInterval] = minutes.toString() }
    }
}
