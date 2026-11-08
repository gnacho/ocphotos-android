package app.ocphotos.di

import android.content.Context
import androidx.room.Room
import app.ocphotos.data.local.PhotosDatabase
import app.ocphotos.data.remote.ApiClient
import app.ocphotos.data.remote.Credentials
import app.ocphotos.data.repo.PhotosRepository
import app.ocphotos.data.session.DataStoreSessionStore
import app.ocphotos.data.session.SessionStore
import app.ocphotos.data.settings.DataStoreSettingsStore
import app.ocphotos.data.settings.SettingsStore
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient

// Manual DI (no Hilt for the MVP): a single graph owned by the Application.
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: PhotosDatabase = Room.databaseBuilder(
        appContext,
        PhotosDatabase::class.java,
        "ocphotos.db",
    ).build()

    val sessionStore: SessionStore = DataStoreSessionStore(appContext)

    val settingsStore: SettingsStore = DataStoreSettingsStore(appContext)

    // Current credentials, updated by the repository on bind(). Images (Coil) and
    // original downloads use the same OkHttp + AuthInterceptor so they carry auth.
    @Volatile private var credentials: Credentials? = null

    val httpClient: OkHttpClient = ApiClient.httpClient { credentials }

    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(appContext)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { httpClient }))
            }
            .build()
    }

    val repository: PhotosRepository = PhotosRepository(
        sessionStore = sessionStore,
        assetDao = database.assetDao(),
        albumDao = database.albumDao(),
        apiFactory = { session ->
            ApiClient.create(session.baseUrl) { Credentials(session.username, session.token) }
        },
        httpClient = httpClient,
        onCredentialsChange = { credentials = it },
    )
}
