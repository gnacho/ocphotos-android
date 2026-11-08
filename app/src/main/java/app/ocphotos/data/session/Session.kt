package app.ocphotos.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Session(
    val serverUrl: String,
    val username: String,
    val token: String,
) {
    // API namespace for photos: the proxy does NOT strip /ocphotos-api/ (SPEC §4.3).
    val baseUrl: String
        get() = serverUrl.trimEnd('/') + "/ocphotos-api/api/"
}

interface SessionStore {
    val session: Flow<Session?>
    suspend fun save(session: Session)
    suspend fun clear()
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class DataStoreSessionStore(private val context: Context) : SessionStore {
    private val keyUrl = stringPreferencesKey("server_url")
    private val keyUser = stringPreferencesKey("username")
    private val tokenKey = "token"

    // The app password is encrypted with a Keystore-backed master key; server URL
    // and username stay in plain DataStore (not secrets).
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "ocphotos_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override val session: Flow<Session?> = context.dataStore.data.map { prefs ->
        val url = prefs[keyUrl] ?: return@map null
        val user = prefs[keyUser] ?: return@map null
        // Degrade to "signed out" if the token cannot be decrypted (e.g. restored
        // backup without the Keystore key) instead of crashing the collector.
        val token = runCatching { encryptedPrefs.getString(tokenKey, null) }.getOrNull()
            ?: return@map null
        Session(url, user, token)
    }

    override suspend fun save(session: Session) {
        encryptedPrefs.edit().putString(tokenKey, session.token).commit()
        context.dataStore.edit { prefs ->
            prefs[keyUrl] = session.serverUrl
            prefs[keyUser] = session.username
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
        encryptedPrefs.edit().remove(tokenKey).commit()
    }
}
